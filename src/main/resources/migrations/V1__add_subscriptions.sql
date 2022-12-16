create table subscriptions (
    chat_id bigint not null,
    team_id integer not null,

    primary key (chat_id, team_id)
);
