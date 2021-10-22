alter table users
    add column language char(2) default 'en';
update users
set language = Default;