drop table if exists todos cascade;
drop table if exists admin_users cascade;
drop table if exists normal_users cascade;

create table NormalUsers (
    id serial primary key,
    username text not null unique,
    password text not null
);

create table AdminUsers (
    code text primary key
);

create table Todos (
    id serial primary key,
    title text not null,
    complete boolean not null,
    ownerId int not null references NormalUsers (id)
);

insert into NormalUsers (username, password)
values
    ('john_doe', 'password123'),
    ('jane_smith', 'secure456'),
    ('alice_wonder', 'wonderland789');

insert into AdminUsers (code)
values
    ('pass'),
    ('word');

insert into Todos (title, complete, ownerId)
values
    ('Buy groceries', false, 1),
    ('Submit assignment', true, 1),
    ('Call mom', false, 2),
    ('Workout', true, 3);
