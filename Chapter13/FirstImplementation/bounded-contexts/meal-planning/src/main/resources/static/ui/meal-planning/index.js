/**
 * Micro-UI of Meal Planning (app-shell/MICRO-UI.md): the cook's meal plans and one meal plan.
 * Calls only the Meal Planning API (contracts/openapi/meal-planning.openapi.yaml). A course's recipe is
 * shown only through Recipe Catalog's documented `larder-recipe-catalog-card`; recipes are chosen through
 * `larder:pick-recipe` → AppShell → `recipePicked(...)`.
 *
 * Spelling of this context: Meal is lower case (`dinner`); the diet filter is a free string that the
 * server reads ignoring case. Only the caller's own plans are listed. A plan's courses, once given, stay
 * 1..10 - the last course cannot be removed, only replaced.
 */
import { ApiError, LarderElement, define, errorMessage, fmt, html } from '/app-shell/kit.js';

const CONTEXT = 'meal-planning';
/** Contract enum Meal of Meal Planning (lower case). */
const MEALS = ['breakfast', 'lunch', 'dinner', 'supper'];
/** Diets a plan can be searched by (derived by the server from the courses' recipes). */
const DIETS = [['vegetarian', 'Vegetarian'], ['vegan', 'Vegan'], ['normal', 'Everything (normal)']];
const MAX_COURSES = 10;

/** Rule-violation codes of Meal Planning → a hint for the cook. */
const HINTS = {
  UNKNOWN_RECIPE: 'That recipe is not in the catalog (anymore) - please pick another one.',
  UNKNOWN_DIET: 'Please choose one of the listed diets.',
};

const fill = (element, template) => {
  if (element) element.innerHTML = template.__html;
};
const idFrom = (location) => (location ? location.split(/[?#]/)[0].replace(/\/+$/, '').split('/').pop() : undefined);
const plural = (count, one, many) => `${count} ${count === 1 ? one : many}`;

function formError(error) {
  if (!error) return html``;
  const code = error instanceof ApiError && error.status < 500 ? error.code : null;
  return html`<div class="error" role="alert">${errorMessage(error)}${HINTS[code] ? html` ${HINTS[code]}` : ''}
    ${code ? html`<span class="code">${code}</span>` : ''}</div>`;
}

const SHARED_STYLES = `
  .code { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; font-size: .78rem; opacity: .8; margin-left: 4px; }
  .hint { font-weight: 400; font-size: .8rem; color: var(--larder-text-muted); }
  .fields { display: grid; gap: 14px; grid-template-columns: repeat(auto-fit, minmax(min(100%, 200px), 1fr)); align-items: start; }
  .fields .wide { grid-column: 1 / -1; }
  .actions { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; align-items: center; }
  @media (max-width: 480px) { .actions .btn { flex: 1 1 auto; } }
  .saved { color: var(--larder-herb); font-weight: 600; font-size: .9rem; }
`;

// ---------------------------------------------------------------- plans

/** `larder-meal-planning-plans`: the cook's meal plans, search by diet and occasion, "New plan". */
class MealPlans extends LarderElement {
  static styles = `${SHARED_STYLES}
    .intro p { color: var(--larder-text-muted); margin: 0; }
    .filters { display: grid; gap: 12px; grid-template-columns: repeat(auto-fit, minmax(min(100%, 200px), 1fr)); align-items: end; }
    .filters .buttons { display: flex; gap: 8px; flex-wrap: wrap; }
    .plan { display: flex; flex-direction: column; gap: 10px; }
    .plan:focus-visible { outline: 3px solid var(--larder-focus); outline-offset: 2px; }
    .plan h3 { margin: 0; }
    .plan .untitled { color: var(--larder-text-muted); font-style: italic; }
    .plan .serve { color: var(--larder-text-muted); font-size: .9rem; margin: 0;
                   display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
    .plan .courses { margin-top: auto; font-size: .88rem; font-weight: 600; color: var(--larder-accent-strong); }
    .count { color: var(--larder-text-muted); font-size: .9rem; font-weight: 600; }
  `;

  constructor() {
    super();
    this.sequence = 0;
    this.on('click', '[data-action=new]', () => this.createPlan());
    this.on('submit', 'form', (event) => {
      event.preventDefault();
      this.search();
    });
    this.on('reset', 'form', () => setTimeout(() => this.search()));
    this.on('change', 'select', () => this.search());
    const open = (target) => this.navigate(`#/meal-plans/${target.dataset.plan}`);
    this.on('click', '[data-plan]', (event, target) => open(target));
    this.on('keydown', '[data-plan]', (event, target) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        open(target);
      }
    });
  }

  connectedCallback() {
    if (this.rendered) return;
    this.rendered = true;
    this.render(html`
      <div class="stack">
        <div class="spread intro">
          <div><h1>Meal plans</h1><p>Plan what you serve - course by course, for every occasion.</p></div>
          <button class="btn btn-primary" data-action="new">New plan</button>
        </div>
        <div id="create-error"></div>
        <div class="card">
          <form class="filters" role="search">
            <label>Diet
              <select name="diet"><option value="">Any diet</option>
                ${DIETS.map(([value, label]) => html`<option value="${value}">${label}</option>`)}</select>
              <span class="hint">Every course suits this diet</span></label>
            <label>Occasion
              <input name="occasion" type="search" maxlength="200" placeholder="e.g. parents in law visiting" autocomplete="off">
              <span class="hint">The whole occasion, upper/lower case does not matter</span></label>
            <div class="buttons">
              <button class="btn btn-primary" type="submit">Search</button>
              <button class="btn" type="reset">Clear</button>
            </div>
          </form>
        </div>
        <div id="results" aria-live="polite"></div>
      </div>`);
    this.search();
  }

  async search() {
    const form = this.$('form');
    const diet = form.elements.diet.value || undefined;
    const occasion = form.elements.occasion.value.trim() || undefined;
    const sequence = ++this.sequence;
    fill(this.$('#results'), html`<p class="loading">Fetching your meal plans…</p>`);
    try {
      const { body } = await this.api(CONTEXT, '/meal-plans', { query: { diet, occasion } });
      if (sequence !== this.sequence) return;
      const plans = Array.isArray(body) ? body : [];
      if (!plans.length) {
        fill(this.$('#results'), diet || occasion
          ? html`<div class="card empty">No meal plan matches. Plans without courses have no diet yet.</div>`
          : html`<div class="card empty"><p>You have no meal plans yet.</p>
              <p><button class="btn btn-primary" data-action="new">Plan your first meal</button></p></div>`);
        return;
      }
      fill(this.$('#results'), html`<div class="stack">
        <span class="count">${plural(plans.length, 'meal plan', 'meal plans')}</span>
        <div class="grid">${plans.map((plan) => this.planCard(plan))}</div></div>`);
    } catch (error) {
      if (sequence !== this.sequence) return;
      fill(this.$('#results'), formError(error));
    }
  }

  planCard(plan) {
    const courses = plan.courses?.length || 0;
    return html`<div class="card clickable plan" role="link" tabindex="0" data-plan="${plan.mealPlanId}"
        aria-label="${plan.occasion || 'Untitled meal plan'}">
      ${plan.occasion ? html`<h3>${plan.occasion}</h3>` : html`<h3 class="untitled">Untitled plan</h3>`}
      <div class="row">
        ${plan.meal ? html`<span class="badge accent">${fmt.label(plan.meal)}</span>` : ''}
        ${plan.servings ? html`<span class="badge">${plural(plan.servings, 'serving', 'servings')}</span>` : ''}
      </div>
      ${plan.howToServe ? html`<p class="serve">${plan.howToServe}</p>` : ''}
      <span class="courses">${courses ? plural(courses, 'course', 'courses') : 'No courses yet'}</span>
    </div>`;
  }

  /** POST /meal-plans with an empty plan; the id is the last segment of the Location header. */
  async createPlan() {
    const buttons = this.shadowRoot.querySelectorAll('[data-action=new]');
    buttons.forEach((button) => { button.disabled = true; });
    fill(this.$('#create-error'), html``);
    try {
      const response = await this.api(CONTEXT, '/meal-plans', { method: 'POST', body: {} });
      const id = idFrom(response.location) || idFrom(response.body?.mealPlanLink);
      if (!id) throw new Error('The plan was created, but Larder did not say where. Please look for it in the list.');
      this.navigate(`#/meal-plans/${id}`);
    } catch (error) {
      fill(this.$('#create-error'), formError(error));
      buttons.forEach((button) => { button.disabled = false; });
    }
  }
}

// ---------------------------------------------------------------- one plan

/**
 * `larder-meal-planning-plan` (`plan`): edit occasion, servings, meal, how to serve and the courses.
 * Details are saved with "Save"; every change of the courses is saved at once (PATCH replaces the list).
 */
class MealPlanView extends LarderElement {
  static styles = `${SHARED_STYLES}
    .back { background: none; border: none; padding: 0; font: inherit; color: var(--larder-accent-strong); cursor: pointer;
            font-weight: 600; }
    .back:hover { text-decoration: underline; }
    h1 .untitled { color: var(--larder-text-muted); font-style: italic; }
    .layout { display: grid; gap: 24px; grid-template-columns: minmax(0, 1fr) minmax(0, 1.25fr); align-items: start; }
    @media (max-width: 860px) { .layout { grid-template-columns: 1fr; } }
    .section-head { display: flex; justify-content: space-between; align-items: baseline; gap: 8px; flex-wrap: wrap; margin-bottom: 12px; }
    .section-head h2 { margin: 0; }
    ol.courses { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 12px; }
    .course { display: grid; grid-template-columns: 84px minmax(0, 1fr); gap: 12px; align-items: center; padding: 12px;
              border-radius: var(--larder-radius-small); background: var(--larder-surface-muted); }
    .course label { font-size: .75rem; }
    .course input { width: 100%; text-align: center; font-weight: 700; }
    .course .tools { grid-column: 2; display: flex; gap: 6px; flex-wrap: wrap; justify-content: flex-end; }
    .course .together { grid-column: 1 / -1; font-size: .8rem; color: var(--larder-text-muted); margin: -4px 0 0; }
    @media (max-width: 480px) {
      .course { grid-template-columns: 64px minmax(0, 1fr); }
      .course .tools { grid-column: 1 / -1; }
      .course .tools .btn { flex: 1 1 auto; }
    }
    .fallback { padding: 10px 14px; background: var(--larder-surface); border-radius: var(--larder-radius-small); display: block; }
    .confirm { border: 1px solid var(--larder-danger); }
    .danger-zone { display: flex; justify-content: flex-end; }
    .course-actions { margin-top: 14px; }
  `;

  static observedAttributes = ['plan'];

  constructor() {
    super();
    this.sequence = 0;
    this.names = new Map(); // recipeId → name, from recipePicked - shown if the card is not available
    this.on('click', '[data-action=back]', () => this.navigate('#/meal-plans'));
    this.on('submit', 'form.details', (event) => {
      event.preventDefault();
      this.saveDetails(event.target);
    });
    this.on('click', '[data-action=add-course]', () => this.emit('larder:pick-recipe', { request: { action: 'add' } }));
    this.on('click', '[data-replace]', (event, target) => this.emit('larder:pick-recipe', {
      request: { action: 'replace', index: Number(target.dataset.replace) },
    }));
    this.on('click', '[data-remove]', (event, target) => this.removeCourse(Number(target.dataset.remove)));
    this.on('change', 'input[data-step]', (event, target) => this.changeStep(Number(target.dataset.step), target));
    this.on('click', '[data-action=ask-delete]', () => this.showDanger({ confirming: true }));
    this.on('click', '[data-action=keep]', () => this.showDanger({ confirming: false }));
    this.on('click', '[data-action=delete]', () => this.deletePlan());
  }

  connectedCallback() {
    this.load();
  }

  attributeChangedCallback(name, before, after) {
    if (before !== after && this.isConnected) this.load();
  }

  async load() {
    const id = this.getAttribute('plan');
    const sequence = ++this.sequence;
    this.plan = null;
    if (!id) {
      this.render(html`<div class="card empty">No meal plan chosen.</div>`);
      return;
    }
    this.render(html`<div class="card"><p class="loading">Fetching the meal plan…</p></div>`);
    try {
      const { body } = await this.api(CONTEXT, `/meal-plans/${encodeURIComponent(id)}`);
      if (sequence !== this.sequence) return;
      this.plan = body;
      this.show();
    } catch (error) {
      if (sequence !== this.sequence) return;
      this.render(html`<div class="card stack">${formError(error)}
        <div><button class="btn" data-action="back">Back to meal plans</button></div></div>`);
    }
  }

  show() {
    const plan = this.plan;
    const mealOptions = MEALS.map((meal) => html`<option value="${meal}" ${meal === plan.meal ? 'selected' : ''}>${fmt.label(meal)}</option>`);
    this.render(html`
      <div class="stack">
        <div><button class="back" data-action="back">← All meal plans</button></div>
        <h1 id="title"></h1>
        <div class="layout">
          <section class="card">
            <div class="section-head"><h2>The occasion</h2></div>
            <form class="details stack" autocomplete="off">
              <div class="fields">
                <label class="wide">Occasion
                  <input name="occasion" maxlength="200" value="${plan.occasion || ''}" placeholder="e.g. parents in law visiting"
                    ${plan.occasion ? 'required' : ''}></label>
                <label>Servings
                  <input name="servings" type="number" inputmode="numeric" min="1" max="999" step="1" value="${plan.servings ?? ''}"
                    placeholder="6" ${plan.servings ? 'required' : ''}></label>
                <label>Meal
                  <select name="meal" ${plan.meal ? 'required' : ''}>
                    <option value="" ${plan.meal ? 'disabled' : 'selected'}>Choose a meal</option>
                    ${mealOptions}</select></label>
                <label class="wide">How to serve <span class="hint">optional</span>
                  <textarea name="howToServe" maxlength="2000"
                    placeholder="e.g. hold course 2 warm while serving the soup">${plan.howToServe || ''}</textarea></label>
              </div>
              <div id="details-error"></div>
              <div class="actions"><span id="details-status" aria-live="polite"></span>
                <button class="btn btn-primary" type="submit" data-role="save-details">Save</button></div>
            </form>
          </section>
          <section class="card">
            <div class="section-head"><h2>Courses</h2><span class="hint">Saved as soon as you change them</span></div>
            <div id="courses"></div>
          </section>
        </div>
        <div id="danger"></div>
      </div>`);
    this.showTitle();
    this.showCourses();
    this.showDanger({ confirming: false });
  }

  showTitle() {
    fill(this.$('#title'), this.plan.occasion ? html`${this.plan.occasion}` : html`<span class="untitled">New meal plan</span>`);
  }

  /** The courses ordered by step (stable); `index` is the position in the plan's own list. */
  orderedCourses() {
    return (this.plan.courses || []).map((course, index) => ({ course, index }))
      .sort((a, b) => a.course.step - b.course.step || a.index - b.index);
  }

  showCourses({ error = null, busy = false, saved = false } = {}) {
    const ordered = this.orderedCourses();
    const count = ordered.length;
    const stepCount = (step) => ordered.filter(({ course }) => course.step === step).length;
    fill(this.$('#courses'), html`
      ${count ? html`<ol class="courses">${ordered.map(({ course, index }) => html`
        <li class="course">
          <label>Course
            <input type="number" inputmode="numeric" min="1" max="99" step="1" required value="${course.step}"
              data-step="${index}" ${busy ? 'disabled' : ''} aria-label="Serving order of this course"></label>
          <larder-recipe-catalog-card recipe="${course.meal.recipe}" compact>
            <span class="fallback">${this.names.get(course.meal.recipe) || 'Recipe'}</span>
          </larder-recipe-catalog-card>
          ${stepCount(course.step) > 1 ? html`<p class="together">Served together with the other course ${course.step} dishes</p>` : ''}
          <div class="tools">
            <button class="btn" type="button" data-replace="${index}" ${busy ? 'disabled' : ''}>Change recipe</button>
            <button class="btn btn-danger" type="button" data-remove="${index}" ${busy || count <= 1 ? 'disabled' : ''}
              title="${count <= 1 ? 'A plan keeps at least one course once it has courses - change the recipe instead' : 'Remove this course'}">Remove</button>
          </div>
        </li>`)}</ol>`
        : html`<div class="empty">No courses yet. Add the first dish of your meal.</div>`}
      ${error ? formError(error) : ''}
      <div class="actions course-actions">
        ${busy ? html`<span class="loading small">Saving…</span>` : saved ? html`<span class="saved">Saved</span>` : ''}
        <button class="btn btn-primary" type="button" data-action="add-course" ${busy || count >= MAX_COURSES ? 'disabled' : ''}
          title="${count >= MAX_COURSES ? `A plan has at most ${MAX_COURSES} courses` : ''}">+ Add a course</button>
      </div>`);
  }

  showDanger({ confirming = false, deleting = false, error = null } = {}) {
    fill(this.$('#danger'), confirming
      ? html`<section class="card confirm stack" role="alertdialog" aria-labelledby="delete-title">
          <h3 id="delete-title">Delete this meal plan?</h3>
          <p class="muted">The plan and its courses are gone for good. The recipes stay in the catalog.</p>
          ${formError(error)}
          <div class="row">
            <button class="btn btn-danger" data-action="delete" ${deleting ? 'disabled' : ''}>${deleting ? 'Deleting…' : 'Yes, delete it'}</button>
            <button class="btn" data-action="keep" ${deleting ? 'disabled' : ''}>Keep it</button>
          </div></section>`
      : html`<div class="danger-zone"><button class="btn btn-danger" data-action="ask-delete">Delete meal plan</button></div>`);
  }

  // ---------------------------------------------------------------- details

  async saveDetails(form) {
    const plan = this.plan;
    const occasion = form.elements.occasion.value.trim();
    const servings = form.elements.servings.value === '' ? null : Number(form.elements.servings.value);
    const meal = form.elements.meal.value;
    const howToServe = form.elements.howToServe.value.trim();
    fill(this.$('#details-error'), html``);
    fill(this.$('#details-status'), html``);

    // Occasion, servings and meal can be changed but not removed (the contract allows no null for them).
    const problems = [];
    if (!occasion && plan.occasion) problems.push('The occasion can be changed, but not removed.');
    if (servings === null && plan.servings) problems.push('The servings can be changed, but not removed.');
    if (problems.length) {
      fill(this.$('#details-error'), html`<div class="error" role="alert">${problems.join(' ')}</div>`);
      return;
    }

    // PATCH changes only the given properties; howToServe: null removes the serving instructions.
    const change = {};
    if (occasion && occasion !== plan.occasion) change.occasion = occasion;
    if (servings !== null && servings !== plan.servings) change.servings = servings;
    if (meal && meal !== plan.meal) change.meal = meal;
    if (howToServe !== (plan.howToServe || '')) change.howToServe = howToServe || null;
    if (!Object.keys(change).length) {
      fill(this.$('#details-status'), html`<span class="muted small">Nothing changed</span>`);
      return;
    }

    const button = this.$('[data-role=save-details]');
    button.disabled = true;
    button.textContent = 'Saving…';
    try {
      await this.api(CONTEXT, `/meal-plans/${encodeURIComponent(plan.mealPlanId)}`, { method: 'PATCH', body: change });
      for (const [key, value] of Object.entries(change)) {
        if (value === null) delete plan[key];
        else plan[key] = value;
      }
      this.showTitle();
      form.elements.occasion.required = Boolean(plan.occasion);
      form.elements.servings.required = Boolean(plan.servings);
      form.elements.meal.required = Boolean(plan.meal);
      const placeholder = form.elements.meal.querySelector('option[value=""]');
      if (placeholder && plan.meal) placeholder.disabled = true;
      fill(this.$('#details-status'), html`<span class="saved">Saved</span>`);
    } catch (error) {
      fill(this.$('#details-error'), formError(error));
    } finally {
      button.disabled = false;
      button.textContent = 'Save';
    }
  }

  // ---------------------------------------------------------------- courses

  /** Called by the AppShell after the cook picked a recipe for `request` (see larder:pick-recipe). */
  recipePicked({ recipeId, name, request } = {}) {
    if (!this.plan || !recipeId) return;
    if (name) this.names.set(recipeId, name);
    const courses = (this.plan.courses || []).map((course) => ({ step: course.step, meal: { recipe: course.meal.recipe } }));
    if (request?.action === 'replace' && courses[request.index]) {
      courses[request.index] = { step: courses[request.index].step, meal: { recipe: recipeId } };
    } else {
      if (courses.length >= MAX_COURSES) return;
      const nextStep = courses.reduce((max, course) => Math.max(max, course.step), 0) + 1;
      courses.push({ step: nextStep, meal: { recipe: recipeId } });
    }
    this.saveCourses(courses);
  }

  removeCourse(index) {
    const courses = this.plan.courses || [];
    if (courses.length <= 1) return;
    this.saveCourses(courses.filter((course, position) => position !== index));
  }

  changeStep(index, input) {
    const step = Number(input.value);
    if (!Number.isInteger(step) || step < 1) {
      input.reportValidity();
      return;
    }
    const courses = (this.plan.courses || []).map((course, position) => (position === index ? { ...course, step } : course));
    this.saveCourses(courses);
  }

  /** PATCH with the whole list - the given list replaces the plan's courses. */
  async saveCourses(courses) {
    const body = { courses: courses.map((course) => ({ step: course.step, meal: { recipe: course.meal.recipe } })) };
    this.showCourses({ busy: true });
    try {
      await this.api(CONTEXT, `/meal-plans/${encodeURIComponent(this.plan.mealPlanId)}`, { method: 'PATCH', body });
      this.plan.courses = body.courses;
      this.showCourses({ saved: true });
    } catch (error) {
      this.showCourses({ error });
    }
  }

  // ---------------------------------------------------------------- delete

  async deletePlan() {
    this.showDanger({ confirming: true, deleting: true });
    try {
      await this.api(CONTEXT, `/meal-plans/${encodeURIComponent(this.plan.mealPlanId)}`, { method: 'DELETE' });
      this.navigate('#/meal-plans');
    } catch (error) {
      this.showDanger({ confirming: true, error });
    }
  }
}

define('larder-meal-planning-plans', MealPlans);
define('larder-meal-planning-plan', MealPlanView);
