-- A meal preparation keeps a snapshot of its recipe's how-to steps (meal_preparation_step), taken when
-- it starts and never changed afterwards: edits of the recipe in Recipe Catalog do not reach a
-- preparation already under way. Only current_step_id changes.
create table meal_preparation (
    preparation_id  uuid primary key,
    cook            uuid not null,
    recipe_id       uuid not null,
    -- One of this preparation's steps; checked by the aggregate (a foreign key would need the
    -- steps before the preparation they belong to).
    current_step_id uuid not null
);

create index meal_preparation_cook_idx on meal_preparation (cook);

create table meal_preparation_step (
    preparation_id  uuid    not null references meal_preparation (preparation_id) on delete cascade,
    how_to_step_id  uuid    not null,
    sequence_number integer not null check (sequence_number >= 1),
    primary key (preparation_id, how_to_step_id),
    unique (preparation_id, sequence_number)
);
