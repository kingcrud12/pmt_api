
    create table project (
        created_at datetime(6) not null,
        author_id CHAR(36) not null,
        id CHAR(36) not null,
        name varchar(100) not null,
        description TEXT,
        primary key (id)
    ) engine=InnoDB;

    create table project_member (
        id CHAR(36) not null,
        project_id CHAR(36) not null,
        user_id CHAR(36) not null,
        role enum ('ADMIN','MEMBER','OBSERVER') not null,
        primary key (id)
    ) engine=InnoDB;

    create table task (
        completion_date date,
        due_date date,
        created_at datetime(6) not null,
        assignee_id CHAR(36) not null,
        id CHAR(36) not null,
        project_id CHAR(36) not null,
        name varchar(100) not null,
        description TEXT,
        priority enum ('CRITICAL','HIGH','LOW','MEDIUM') not null,
        status enum ('ARCHIVED','DONE','IN_PROGRESS','TODO') not null,
        primary key (id)
    ) engine=InnoDB;

    create table task_history (
        modified_at datetime(6) not null,
        id CHAR(36) not null,
        modifier_id CHAR(36) not null,
        task_id CHAR(36) not null,
        change_type varchar(50) not null,
        new_value TEXT,
        old_value TEXT,
        primary key (id)
    ) engine=InnoDB;

    create table user (
        id CHAR(36) not null,
        email varchar(255) not null,
        first_name varchar(255) not null,
        last_name varchar(255) not null,
        password varchar(255) not null,
        role varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    alter table project_member 
       add constraint unique_project_member unique (project_id, user_id);

    alter table user 
       add constraint UKob8kqyqqgmefl0aco34akdtpe unique (email);

    alter table project 
       add constraint FKte6bms4bq1ixfhn024qtysmcg 
       foreign key (author_id) 
       references user (id);

    alter table project_member 
       add constraint FK103dwxad12nbaxtmnwus4eft2 
       foreign key (project_id) 
       references project (id);

    alter table project_member 
       add constraint FK6s59w9jalg0dperffu3ri91or 
       foreign key (user_id) 
       references user (id);

    alter table task 
       add constraint FKsrodfgrekcvv8ksyslehr53j8 
       foreign key (assignee_id) 
       references user (id);

    alter table task 
       add constraint FKk8qrwowg31kx7hp93sru1pdqa 
       foreign key (project_id) 
       references project (id);

    alter table task_history 
       add constraint FKj304fjm2ls6x3srwt122l9tn2 
       foreign key (modifier_id) 
       references user (id);

    alter table task_history 
       add constraint FKer57q2libi1e9njpj6faoxd2i 
       foreign key (task_id) 
       references task (id);
