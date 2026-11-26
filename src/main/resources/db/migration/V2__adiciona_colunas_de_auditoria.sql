alter table autor
    add column data_cadastro timestamp,
    add column data_atualizacao timestamp,
    add column id_usuario uuid;

alter table livro
    add column data_cadastro timestamp,
    add column data_atualizacao timestamp,
    add column id_usuario uuid;