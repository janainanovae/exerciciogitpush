// ============================================================
// Passo 2: consultas com find (execute depois do script 01)
// Executar com:  mongosh --file 02_find.js
// ============================================================

db = db.getSiblingDB("loja");

// 1) Todos os produtos
print("--- Todos os produtos ---");
db.produtos.find().forEach(doc => printjson(doc));

// 2) Filtro por igualdade: produtos da categoria "Informática"
print("--- Categoria Informática ---");
db.produtos.find({ categoria: "Informática" }).forEach(doc => printjson(doc));

// 3) Filtro com operador de comparação: preço maior que 100
print("--- Preço maior que 100 ---");
db.produtos.find({ preco: { $gt: 100 } }).forEach(doc => printjson(doc));

// 4) Projeção (só nome e preço) e ordenação (do mais caro para o mais barato)
print("--- Nome e preço, do mais caro para o mais barato ---");
db.produtos.find({}, { _id: 0, nome: 1, preco: 1 }).sort({ preco: -1 }).forEach(doc => printjson(doc));

// 5) findOne: apenas um documento
print("--- findOne: Mouse sem fio ---");
printjson(db.produtos.findOne({ nome: "Mouse sem fio" }));
