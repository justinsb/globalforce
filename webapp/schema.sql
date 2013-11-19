create table human_worker (id serial primary key, login varchar, name varchar);

create table task (id bigint primary key, problem varchar, objectid varchar, sequence int, input varchar, worker int, decision varchar);

create index task_lookup ON task (problem, objectid, sequence);

create index task_queue ON task (id, problem) WHERE worker IS NULL;
