// ============================================================
// Passo 4: remoções com delete (execute depois dos scripts 01, 02 e 03)
// Executar com:  mongosh --file 04_delete.js
// ============================================================

db = db.getSiblingDB("loja");

// 1) deleteOne: remove um produto pelo nome
print("--- deleteOne: Luminária de mesa ---");
printjson(db.produtos.deleteOne({ nome: "Luminária de mesa" }));

// 2) deleteMany: remove todos os produtos da categoria "Móveis"
print("--- deleteMany: categoria Móveis ---");
printjson(db.produtos.deleteMany({ categoria: "Móveis" }));

// Conferência: devem restar 3 produtos (Notebook, Mouse sem fio e Teclado mecânico)
print("--- Produtos que restaram ---");
db.produtos.find().forEach(doc => printjson(doc));
print("Total: " + db.produtos.countDocuments());
