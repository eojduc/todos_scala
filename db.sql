-- drop table if exists todos cascade;
-- drop table if exists admin_users cascade;
-- drop table if exists normal_users cascade;

create table normal_users (
    id serial primary key,
    username text not null unique,
    password text not null
);

create table admin_users (
    code text primary key
);

create table todos (
    id serial primary key,
    title text not null,
    complete boolean not null,
    ownerId int not null references normal_users (id)
);

insert into normal_users (username, password)
values
    ('john_doe', 'password123'),
    ('jane_smith', 'secure456'),
    ('alice_wonder', 'wonderland789');

insert into admin_users (code)
values
    ('pass'),
    ('word');

insert into todos (title, complete, ownerId)
values
    ('Buy groceries', false, 1),
    ('Submit assignment', true, 1),
    ('Call mom', false, 2),
    ('Workout', true, 3);
