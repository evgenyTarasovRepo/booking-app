--Insert data for tests
insert into users(id, first_name, last_name, email, created_at, is_deleted)
values ('901737a7-2dc4-477e-adaa-7d1df44623dd', 'UserName-1', 'UserLastName-1', 'userEmail@gmail.com', now(),false);

insert into users(id, first_name, last_name, email, created_at, is_deleted)
values ('501737a7-2dc4-466e-adaa-7d1df33623bf', 'UserName-2', 'UserLastName-2', 'testEmail@corp.com', now(), false);

insert into users(id, first_name, last_name, email, created_at, is_deleted)
values ('96abd400-20d2-409f-939a-6418174b44d5', 'UserName-3', 'UserLastName-3', 'myEmail@blabla.com', now(), false);

insert into users(id, first_name, last_name, email, created_at, is_deleted)
values ('a621a74a-b86d-4da8-9271-a22bd22465ea', 'UserName-4', 'UserLastName-4', 'randomEmail@yahoo.com', now(), false);

insert into users(id, first_name, last_name, email, created_at, is_deleted)
values ('3308baee-ec23-4173-8a8f-2ea30a3ff097', 'UserName-5', 'UserLastName-5', 'firstEmail@tar.com', now(), false);

insert into users(id, first_name, last_name, email, created_at, is_deleted)
values ('ef41f5db-1ffd-41ba-93b4-8b9bda924f48', 'UserName-6', 'UserLastName-6', 'deletedUser@del.com', now(),true);

insert into users(id, first_name, last_name, email, created_at, is_deleted)
values ('24790ea0-c47f-46b2-b1fe-c8bd4ba60edb', 'UserName-7', 'UserLastName-7', 'deletedUser-2@del.com', now(), true);