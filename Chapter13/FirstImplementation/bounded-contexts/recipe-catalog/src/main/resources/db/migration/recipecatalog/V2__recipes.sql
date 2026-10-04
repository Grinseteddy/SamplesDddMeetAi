-- A recipe is stored as a whole: the recipe row plus its further images, ingredients and how-to steps.
-- Child rows belong to exactly one recipe and go with it (on delete cascade).
create table recipe (
    recipe_id           uuid primary key,
    owner               uuid         not null,
    name                varchar(200) not null check (length(trim(name)) > 0),
    subtitle            varchar(300),
    main_image          text,
    -- HH:MM of the contract, kept as minutes (00:00 .. 99:59)
    preparation_minutes integer      not null check (preparation_minutes between 0 and 99 * 60 + 59),
    servings            integer      not null check (servings >= 1),
    meal                varchar(16)  not null check (meal in ('BREAKFAST', 'LUNCH', 'DINNER', 'SUPPER')),
    diet                varchar(16)  not null check (diet in ('VEGETARIAN', 'VEGAN', 'NORMAL'))
);

create index recipe_owner_idx on recipe (owner);
create index recipe_meal_diet_idx on recipe (meal, diet);

create table recipe_further_image (
    recipe_id uuid    not null references recipe (recipe_id) on delete cascade,
    position  integer not null check (position between 0 and 9),
    image     text    not null,
    primary key (recipe_id, position)
);

create table ingredient (
    ingredient_id uuid primary key,
    recipe_id     uuid           not null references recipe (recipe_id) on delete cascade,
    position      integer        not null,
    name          varchar(100)   not null check (length(trim(name)) > 0),
    value         numeric        not null check (value > 0),
    unit          varchar(16)    not null check (unit in ('PIECE', 'CUP', 'TABLE_SPOON', 'TEA_SPOON', 'FLUID_OUNCES',
                                                         'PINT', 'QUART', 'POUND', 'KILOGRAM', 'GRAM', 'LITER',
                                                         'MILLILITER', 'PINCH')),
    unique (recipe_id, position)
);

-- Search by ingredient name ignores case.
create index ingredient_name_idx on ingredient (lower(name));

create table how_to_step (
    how_to_step_id  uuid primary key,
    recipe_id       uuid          not null references recipe (recipe_id) on delete cascade,
    sequence_number integer       not null check (sequence_number >= 1),
    description     varchar(2000) not null check (length(trim(description)) > 0),
    illustration    text,
    unique (recipe_id, sequence_number)
);
