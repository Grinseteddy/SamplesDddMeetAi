-- A meal plan is stored as a whole: the meal plan row plus its courses.
-- Every part except the owner is optional, because a plan is set up empty and filled step by step.
create table meal_plan (
    meal_plan_id uuid primary key,
    owner        uuid not null,
    occasion     varchar(200) check (length(trim(occasion)) > 0),
    servings     integer check (servings >= 1),
    meal         varchar(16) check (meal in ('BREAKFAST', 'LUNCH', 'DINNER', 'SUPPER')),
    how_to_serve varchar(2000) check (length(trim(how_to_serve)) > 0)
);

create index meal_plan_owner_idx on meal_plan (owner);
-- Search by occasion ignores case.
create index meal_plan_occasion_idx on meal_plan (owner, lower(occasion));

-- Courses belong to exactly one meal plan and go with it. diet is a snapshot of the referenced
-- recipe's diet in the Recipe Catalog, taken when the courses were set; recipe_id is no foreign key,
-- the recipe lives in another Bounded Context.
create table course (
    meal_plan_id uuid    not null references meal_plan (meal_plan_id) on delete cascade,
    position     integer not null check (position between 0 and 9),
    step         integer not null check (step >= 1),
    recipe_id    uuid    not null,
    diet         varchar(16) not null check (diet in ('NORMAL', 'VEGETARIAN', 'VEGAN')),
    primary key (meal_plan_id, position)
);
