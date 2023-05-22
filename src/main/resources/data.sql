INSERT INTO LEVEL (name, policy, role)
VALUES ('고마운분', 0, 'ROLE_NORMAL'),
       ('귀한분', 5, 'ROLE_VIP'),
       ('더귀한분', 15, 'ROLE_VIP'),
       ('천생연분', 20, 'ROLE_VIP'),
       ('관리자', -1, 'ROLE_ADMIN');

INSERT INTO USERS (email, username, password, level_id)
VALUES ('test@naver.com', 'test', '1234', 1);