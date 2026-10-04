package org.larder.sharing.application;

import static org.larder.sharing.domain.ThanksRuleViolationException.THANKS_ALREADY_GIVEN;

import java.time.Clock;
import java.util.Collection;
import java.util.List;

import org.larder.sharing.domain.CookId;
import org.larder.sharing.domain.Help;
import org.larder.sharing.domain.HelpId;
import org.larder.sharing.domain.Picture;
import org.larder.sharing.domain.Recipient;
import org.larder.sharing.domain.Thanks;
import org.larder.sharing.domain.ThanksId;
import org.larder.sharing.domain.ThanksRevision;
import org.larder.sharing.domain.ThanksRuleViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of Sharing. Thanks are visible to every cook; only their giver changes or withdraws them.
 *
 * <p>Giving thanks checks, in this order, and stops at the first failure:
 * <ol>
 *   <li>the thanks' own rules - recipients, text, picture link (400, in the domain);</li>
 *   <li>the help exists in Cooking Assistance (400 {@code UNKNOWN_HELP}; 403 if Cooking Assistance refuses);</li>
 *   <li>the caller received the help - only the requester thanks for it (403);</li>
 *   <li>no thanks were given for this help yet (400 {@code THANKS_ALREADY_GIVEN});</li>
 *   <li>the recipients are who gave the help (400 {@code RECIPIENT_NOT_HELPER});</li>
 *   <li>the picture is an image of Media (400 {@code UNKNOWN_PICTURE});</li>
 *   <li>the giver consented to the use of their photos in public thanks (400 {@code PICTURE_WITHOUT_CONSENT});</li>
 *   <li>every mentioned cook consented to being mentioned (400 {@code MENTION_WITHOUT_CONSENT}).</li>
 * </ol>
 * A change re-checks what it changes: new recipients steps 2, 5 and 8, a new picture steps 6 and 7.
 *
 * <p>No use case holds a database transaction while calling an upstream: look up first, then store
 * atomically ({@link ThanksRepository}); a concurrent change in between is detected by the version.
 */
@Service
public class ThanksService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "sharingTransactionManager";

    private final ThanksRepository thanks;
    private final Helps helps;
    private final Pictures pictures;
    private final Consents consents;
    private final Clock clock;

    public ThanksService(ThanksRepository thanks, Helps helps, Pictures pictures, Consents consents, Clock clock) {
        this.thanks = thanks;
        this.helps = helps;
        this.pictures = pictures;
        this.consents = consents;
        this.clock = clock;
    }

    public Thanks give(CookId caller, HelpId helpId, List<Recipient> recipients, String text, Picture picture) {
        Thanks given = Thanks.give(caller, helpId, recipients, text, picture, clock.instant());
        Help help = receivedHelp(caller, helpId);
        if (thanks.existsForHelp(helpId)) {
            throw new ThanksRuleViolationException(THANKS_ALREADY_GIVEN,
                    "Thanks for help " + helpId.value() + " were already given; change them instead");
        }
        help.verifyAddressedToHelper(given.recipients());
        verifyPicture(caller, given.picture());
        verifyMentionable(given.mentionedCooks());
        thanks.add(given);
        return given;
    }

    public Thanks revise(CookId caller, ThanksId id, ThanksRevision revision) {
        Thanks own = ownThanks(caller, id);
        if (revision.changesNothing()) {
            return own;
        }
        own.revise(revision, clock.instant());
        if (revision.changesRecipients()) {
            Help help = helps.find(own.help()).orElseThrow(() -> new UnknownHelpException(own.help()));
            help.verifyAddressedToHelper(own.recipients());
        }
        if (revision.changesPicture()) {
            verifyPicture(caller, own.picture());
        }
        if (revision.changesRecipients()) {
            verifyMentionable(own.mentionedCooks());
        }
        thanks.update(own);
        return own;
    }

    @Transactional(transactionManager = ThanksService.TRANSACTIONS)
    public void withdraw(CookId caller, ThanksId id) {
        ownThanks(caller, id);
        if (!thanks.remove(id)) {
            throw notFound(id);
        }
    }

    public Thanks thanks(ThanksId id) {
        return thanks.findById(id).orElseThrow(() -> notFound(id));
    }

    public List<Thanks> thanks(ThanksFilter filter) {
        return thanks.find(filter);
    }

    private Help receivedHelp(CookId caller, HelpId helpId) {
        Help help = helps.find(helpId).orElseThrow(() -> new UnknownHelpException(helpId));
        if (!help.isReceivedBy(caller)) {
            throw new NotPermittedException("Only the cook who received help " + helpId.value() + " may thank for it");
        }
        return help;
    }

    private Thanks ownThanks(CookId caller, ThanksId id) {
        Thanks found = thanks(id);
        if (!found.isGivenBy(caller)) {
            throw new NotPermittedException("Only the giver may change or withdraw thanks " + id.value());
        }
        return found;
    }

    private void verifyPicture(CookId giver, Picture picture) {
        if (!pictures.exists(picture.mediaId())) {
            throw new UnknownPictureException(picture);
        }
        if (!consents.isInForce(giver, ConsentPurpose.PHOTOS_IN_PUBLIC_THANKS)) {
            throw ConsentMissingException.toSharePhotos(giver);
        }
    }

    private void verifyMentionable(Collection<CookId> mentioned) {
        for (CookId cook : mentioned) {
            if (!consents.isInForce(cook, ConsentPurpose.MENTION_AS_HELPER)) {
                throw ConsentMissingException.toBeMentioned(cook);
            }
        }
    }

    private static NotFoundException notFound(ThanksId id) {
        return new NotFoundException("Thanks " + id.value() + " not found");
    }
}
