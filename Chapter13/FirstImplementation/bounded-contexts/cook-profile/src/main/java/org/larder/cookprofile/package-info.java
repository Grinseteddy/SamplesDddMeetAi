/**
 * Bounded Context CookProfile. Cooks; anticorruption layer to the IAM.
 *
 * <p>The contract carries no consents at registration, so the generated Consent Management client
 * stays unused for now; the IAM integration (behind an application port) follows once the contract needs it.
 *
 * <ul>
 *   <li>{@code domain} - aggregates, value objects, domain events; no framework code</li>
 *   <li>{@code application} - use cases and ports</li>
 *   <li>{@code adapter.in} - REST (generated from the contract) and message consumers</li>
 *   <li>{@code adapter.out} - persistence, clients of upstream contexts, publishers</li>
 * </ul>
 */
package org.larder.cookprofile;
