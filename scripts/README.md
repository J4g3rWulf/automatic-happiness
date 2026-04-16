# Scripts de administração do RecycleApp

## populate_firestore_v2.py

Script Python para popular e gerenciar a coleção `recycling_points` no Cloud Firestore,
lendo os dados diretamente dos arquivos Excel do projeto (schema v2).

### Estrutura de pastas esperada

```
📁 esta pasta/
├── populate_firestore_v2.py
├── serviceAccountKey.json       ← nunca commitar
├── README.md
└── 📁 dados/
    ├── angra_dos_reis_ecopontos_rio_v2.xlsx
    ├── angra_dos_reis_pev_rio_v2.xlsx
    ├── comlurb_ecopontos_v2.xlsx
    ├── comlurb_pev_rio_v2.xlsx
    ├── duque_de_caxias_ecopontos_rio_v2.xlsx
    ├── light_ecopontos_rio_v2.xlsx
    ├── niteroi_ecopontos_rio_v2.xlsx
    ├── niteroi_pev_rio_v2.xlsx
    └── sao_goncalo_ecopontos_rio_v2.xlsx
```

### Pré-requisitos

- Python 3.x instalado
- Dependências: `pip install firebase-admin pandas openpyxl`
- Arquivo `serviceAccountKey.json` na mesma pasta do script
  (gerado no Firebase Console → Configurações → Contas de serviço → Gerar nova chave privada)

> ⚠️ O `serviceAccountKey.json` **nunca deve ser commitado** — ele já está no `.gitignore`.

---

### Como usar

#### Enviar pontos ao Firestore (uso normal)
```bash
python populate_firestore_v2.py
```
Lê todos os Excel da pasta `dados/` e envia ao Firestore. Idempotente — reexecutar
sobrescreve sem duplicar.

#### Simular sem enviar nada
```bash
python populate_firestore_v2.py --dry-run
```
Mostra o payload do primeiro ponto de cada arquivo. Útil para validar antes de enviar.

#### Apagar IDs do schema antigo antes de enviar
```bash
python populate_firestore_v2.py --delete-old
```
Apaga os IDs listados em `TO_DELETE` no script antes de enviar os novos.
Use na primeira migração do schema v1 para o v2. Após confirmar a limpeza,
esvazie a lista `TO_DELETE` no script.

#### Gerar o fallback estático do app Android
```bash
python populate_firestore_v2.py --gen-fallback
```
Gera o arquivo `RecyclingPointsData.kt` na pasta do script. Copie-o para:
`app/src/main/java/br/recycleapp/data/map/`

> **Por que isso existe?** O app busca pontos em três camadas: Firestore (primário),
> cache local (atualizado automaticamente) e um fallback estático compilado no APK
> usado apenas no primeiro acesso sem internet. O `--gen-fallback` mantém esse
> fallback sincronizado com os Excel quando novos pontos são adicionados.

#### Usar múltiplas flags juntas
```bash
python populate_firestore_v2.py --delete-old --gen-fallback
python populate_firestore_v2.py --dry-run --gen-fallback
```

---

### Adicionando novos municípios ou arquivos Excel

1. Crie o arquivo Excel seguindo a estrutura dos existentes (20 colunas, cabeçalho
   na linha 2, dados a partir da linha 3) e coloque-o na pasta `dados/`
2. Adicione uma entrada na lista `EXCEL_FILES` no script:
```python
{
    "filename": "nova_iguacu_ecopontos_rio_v2.xlsx",
    "section":  "Ecopontos de Nova Iguaçu",
    "var_name": "nova_iguacu_ecopontos",
},
```
3. Adicione o novo `PointType` em `PointType.kt` no projeto Android
4. Execute o script normalmente

---

### Estrutura de uma linha no Excel (schema v2)

| Campo              | Descrição                                      | Exemplo                          |
|--------------------|------------------------------------------------|----------------------------------|
| `id`               | Identificador único kebab-case                 | `ecoponto-comlurb-arara`         |
| `name`             | Nome de exibição                               | `Ecoponto Arará`                 |
| `subtitle`         | Descrição curta do tipo/programa               | `Ecoponto Comlurb`               |
| `type`             | Tipo do ponto — ver tipos válidos abaixo       | `ECOPONTO_COMLURB`               |
| `street`           | Logradouro sem número                          | `Rua Aloysio Amancio`            |
| `number`           | Número ou `s/n`                                | `s/n`                            |
| `neighborhood`     | Bairro                                         | `Benfica`                        |
| `postal_code`      | CEP sem traço (rastreabilidade, não exibido)   | `20550000`                       |
| `reference`        | Referência complementar opcional               | `ao lado da UPA`                 |
| `city`             | Cidade                                         | `Rio de Janeiro`                 |
| `state`            | Estado (sigla)                                 | `RJ`                             |
| `latitude`         | Latitude decimal (número, sem separador milhar)| `-22.889472`                     |
| `longitude`        | Longitude decimal                              | `-43.242306`                     |
| `materials`        | Materiais aceitos, separados por vírgula       | `Vidro, Plástico, Papel, Metal`  |
| `schedule_weekdays`| Horário dias úteis                             | `Segunda - Sexta: 8h às 17h`     |
| `schedule_saturday`| Horário sábado                                 | `Sábado: 8h às 12h`              |
| `schedule_sunday`  | Horário domingo                                | `Domingo: 9h às 12h`             |
| `benefits_program` | Nome do programa de benefícios                 | `EcoCLIN`                        |
| `benefits`         | Benefícios, separados por vírgula              | `Crédito na conta de energia`    |
| `active`           | Ponto ativo?                                   | `TRUE`                           |

### Tipos válidos (`type`)

| Tipo                      | Descrição                              |
|---------------------------|----------------------------------------|
| `PEV_COMLURB`             | Ponto de Entrega Voluntária da Comlurb |
| `ECOPONTO_COMLURB`        | Ecoponto da Comlurb                    |
| `ECOPONTO_LIGHT`          | Ecoponto Light Recicla                 |
| `PEV_NITEROI`             | PUD de Niterói                         |
| `ECOPONTO_NITEROI`        | Ecoponto da CLIN (Niterói)             |
| `ECOPONTO_SAO_GONCALO`    | Ecoponto de São Gonçalo                |
| `ECOPONTO_DUQUE_DE_CAXIAS`| Ecoponto de Duque de Caxias            |
| `PEV_ANGRA_DOS_REIS`      | PEV de Angra dos Reis                  |
| `ECOPONTO_ANGRA_DOS_REIS` | Ecoponto de Angra dos Reis             |

### Materiais válidos

`Vidro`, `Plástico`, `Papel`, `Metal`, `Óleo vegetal`, `Pilhas e baterias`,
`Eletrônicos`, `Lixo domiciliar`, `Orgânico`, `Bens inservíveis`,
`Entulho`, `Pneus`, `Galhadas`
