create table human_worker (id serial primary key, login varchar, name varchar);

create table task (id bigint primary key, organization varchar, problem varchar, objectid varchar, sequence int, input varchar, worker varchar, decision varchar);

create index task_lookup ON task (organization, problem, objectid, sequence);

create index task_queue ON task (organization, id, problem) WHERE worker IS NULL;
