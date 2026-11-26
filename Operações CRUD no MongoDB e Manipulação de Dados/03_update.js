// ============================================================
// Passo 3: atualizações com update (execute depois dos scripts 01 e 02)
// Executar com:  mongosh --file 03_update.js
// ============================================================

db = db.getSiblingDB("loja");

// 1) updateOne + $set: altera o preço do Notebook de 3500,00 para 3299,90
print("--- updateOne: novo preço do Notebook ---");
printjson(db.produtos.updateOne(
  { nome: "Notebook" },
  { $set: { preco: 3299.90 } }
));

// 2) updateMany + $inc: soma 5 unidades ao estoque de todos os produtos da categoria "Móveis"
print("--- updateMany: +5 no estoque dos Móveis ---");
printjson(db.produtos.updateMany(
  { categoria: "Móveis" },
  { $inc: { estoque: 5 } }
));

// 3) updateMany + $set: marca como destaque os produtos com preço acima de 1000
//    (cria o campo "destaque" só nesses documentos: o MongoDB não exige um esquema fixo)
print("--- updateMany: destaque nos produtos acima de 1000 ---");
printjson(db.produtos.updateMany(
  { preco: { $gt: 1000 } },
  { $set: { destaque: true } }
));

// Conferência do resultado
print("--- Produtos depois das atualizações ---");
db.produtos.find().forEach(doc => printjson(doc));
