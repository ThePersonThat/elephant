drop table backups;
drop table databases;
drop table users;

CREATE ROLE customer;

create table users
(
    id         serial primary key,
    login      varchar(200) not null unique,
    password   varchar(200) not null,
    username   varchar(50)  not null unique,
    dbPassword varchar(50)  not null,
    token      varchar(50)  not null,
    role       smallint     not null,
    publicKey  varchar(200) not null,
    privateKey varchar(200) not null unique
);

create table databases
(
    name  varchar(50) not null primary key,
    owner varchar(50) not null references users (username)
);

create table backups
(
    database    varchar(50) not null references databases (name),
    point       varchar(50) not null,
    status      varchar(10) not null,
    "createdAt" timestamp   not null,
    "updatedAt" timestamp   not null,
    id          serial primary key,
    unique (database, point)
);

create table scripts
(
    id          serial
        constraint scripts_pk
            primary key,
    database    varchar   default 50    not null
        constraint scripts_databases_name_fk
            references databases (name)
            on update cascade on delete cascade,
    filename    varchar(1000)           not null,
    path        varchar(100)            not null,
    "createdAt" timestamp default now() not null,
    "updatedAt" timestamp default now() not null
);

create table LOGGER
(
    id         serial
        constraint logger_pk
            primary key,
    ip         varchar(16)             not null,
    database   varchar(60),
    "user"     varchar(10)             not null,
    message    text                    not null,
    created_at timestamp default now() not null
);

