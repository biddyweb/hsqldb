drop table test if exists;
drop table log if exists;
create cached table test(id integer, data varchar(20));
create cached table log(id integer, data varchar(20), op varchar(10));
create trigger trig after insert on test referencing new row as newrow
 for each row when (newrow.id >1)
 insert into log values (newrow.id, newrow.data, 'inserted')
insert into test values(1,'one');
insert into test values(2,'two');
alter table test drop column id cascade;
drop table test;

