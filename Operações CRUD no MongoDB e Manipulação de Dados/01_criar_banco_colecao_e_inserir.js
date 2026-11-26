// ============================================================
// Passo 1: criar o banco "loja", a coleção "produtos" e inserir documentos
// Executar com:  mongosh --file 01_criar_banco_colecao_e_inserir.js
// (ou colar o conteúdo no mongosh / no terminal do MongoDB Compass)
// ============================================================

// Seleciona o banco "loja". No MongoDB ele só passa a existir de verdade quando recebe dados.
// (No mongosh interativo, o equivalente é:  use loja)
db = db.getSiblingDB("loja");

// Recomeça do zero, para o script poder ser executado quantas vezes for preciso
db.produtos.drop();

// Cria a coleção "produtos" explicitamente
db.createCollection("produtos");

// Insere 6 documentos
db.produtos.insertMany([
  { nome: "Notebook",             categoria: "Informática", preco: 3500.00, estoque: 10 },
  { nome: "Mouse sem fio",        categoria: "Informática", preco: 89.90,   estoque: 50 },
  { nome: "Teclado mecânico",     categoria: "Informática", preco: 299.90,  estoque: 25 },
  { nome: "Cadeira de escritório", categoria: "Móveis",     preco: 750.00,  estoque: 8 },
  { nome: "Mesa de escritório",   categoria: "Móveis",      preco: 1100.00, estoque: 5 },
  { nome: "Luminária de mesa",    categoria: "Casa",        preco: 69.90,   estoque: 40 }
]);

// Conferência
print("Banco: " + db.getName());
print("Coleções: " + db.getCollectionNames());
print("Documentos em produtos: " + db.produtos.countDocuments());
