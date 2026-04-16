"""
populate_firestore_v2.py
════════════════════════
Popula a coleção `recycling_points` no Cloud Firestore com todos os pontos
de coleta do RecycleApp, lendo os arquivos Excel do projeto (schema v2).

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
CONFIGURAÇÃO INICIAL (só precisa fazer uma vez)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. Instale as dependências:
       pip install firebase-admin pandas openpyxl

2. No Firebase Console:
       Configurações do projeto → Contas de serviço → Gerar nova chave privada
       Salve o JSON como `serviceAccountKey.json` na mesma pasta deste script.

3. Coloque os arquivos Excel na mesma pasta deste script
   (ou ajuste EXCEL_DIR abaixo para apontar para a pasta correta).

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
USO
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Uso básico (lê todos os Excel e envia ao Firestore):
    python populate_firestore_v2.py

Simular sem enviar nada (mostra o primeiro documento de cada arquivo):
    python populate_firestore_v2.py --dry-run

Apagar IDs antigos (schema v1) antes de enviar:
    python populate_firestore_v2.py --delete-old

Gerar RecyclingPointsData.kt (fallback estático do app Android):
    python populate_firestore_v2.py --gen-fallback

Usar todas as flags juntas:
    python populate_firestore_v2.py --dry-run --delete-old --gen-fallback

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ADICIONANDO NOVOS MUNICÍPIOS OU ARQUIVOS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Para adicionar um novo arquivo Excel ao pipeline:
  1. Crie o arquivo Excel seguindo a estrutura dos existentes (20 colunas,
     cabeçalho na linha 2, dados a partir da linha 3).
  2. Adicione uma entrada na lista EXCEL_FILES abaixo com:
       - filename:    nome do arquivo .xlsx
       - section:     título da seção no RecyclingPointsData.kt gerado
       - var_name:    nome da variável Kotlin (snake_case, sem espaços)
  3. Rode o script normalmente — ele detecta e processa o novo arquivo.
  Não é necessário alterar nenhuma outra parte do código.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SOBRE O --gen-fallback
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
O app Android tem três camadas de dados para os pontos de coleta:

  1. Firestore       → fonte primária, atualizada em runtime
  2. last-known      → cache local (SharedPreferences), atualizado automaticamente
                       sempre que o Firestore responde com sucesso
  3. RecyclingPointsData.kt → fallback estático compilado no APK,
                       usado APENAS no primeiro acesso sem internet

O fallback estático (camada 3) NUNCA é atualizado automaticamente em runtime
— ele é código Kotlin compilado dentro do APK. Para mantê-lo atualizado:
  1. Rode este script com --gen-fallback após atualizar os Excel
  2. Copie o RecyclingPointsData.kt gerado para o projeto Android
  3. Publique uma nova versão do app

Qualquer usuário que já abriu o app com internet pelo menos uma vez
usa o last-known (camada 2) e nunca precisa do fallback estático.
O fallback só importa para novos usuários que instalem o app sem internet.
"""

import sys
import os
from datetime import datetime, timezone

import pandas as pd

# ── Flags de execução ─────────────────────────────────────────────────────────

DRY_RUN      = "--dry-run"      in sys.argv  # imprime sem enviar ao Firestore
DELETE_OLD   = "--delete-old"   in sys.argv  # apaga IDs v1 antes de enviar
GEN_FALLBACK = "--gen-fallback" in sys.argv  # gera RecyclingPointsData.kt

# ── Configuração ──────────────────────────────────────────────────────────────

# Pasta onde estão os arquivos Excel.
EXCEL_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "dados")

COLLECTION = "recycling_points"   # coleção principal no Firestore
OUTPUT_KT  = "RecyclingPointsData.kt"  # arquivo Kotlin gerado pelo --gen-fallback
KT_PACKAGE = "br.recycleapp.data.map"  # package do arquivo Kotlin gerado

# ── Arquivos Excel ────────────────────────────────────────────────────────────
# Cada entrada define um arquivo a ser processado.
# Para adicionar um novo município, basta adicionar uma entrada aqui.
#
# Campos:
#   filename  → nome do arquivo .xlsx (deve estar em EXCEL_DIR)
#   section   → título da seção no RecyclingPointsData.kt gerado
#   var_name  → nome da variável Kotlin (snake_case)

EXCEL_FILES = [
    {
        "filename": "angra_dos_reis_ecopontos_rio_v2.xlsx",
        "section":  "Ecopontos de Angra dos Reis",
        "var_name": "angra_dos_reis_ecopontos",
    },
    {
        "filename": "angra_dos_reis_pev_rio_v2.xlsx",
        "section":  "PEVs de Angra dos Reis",
        "var_name": "angra_dos_reis_pev",
    },
    {
        "filename": "comlurb_ecopontos_v2.xlsx",
        "section":  "Ecopontos Comlurb",
        "var_name": "comlurb_ecopontos",
    },
    {
        "filename": "comlurb_pev_rio_v2.xlsx",
        "section":  "PEVs Comlurb",
        "var_name": "comlurb_pev",
    },
    {
        "filename": "duque_de_caxias_ecopontos_rio_v2.xlsx",
        "section":  "Ecopontos de Duque de Caxias",
        "var_name": "duque_de_caxias_ecopontos",
    },
    {
        "filename": "light_ecopontos_rio_v2.xlsx",
        "section":  "Ecopontos Light Recicla",
        "var_name": "light_ecopontos",
    },
    {
        "filename": "niteroi_ecopontos_rio_v2.xlsx",
        "section":  "Ecopontos de Niterói (CLIN)",
        "var_name": "niteroi_ecopontos",
    },
    {
        "filename": "niteroi_pev_rio_v2.xlsx",
        "section":  "PEVs de Niterói",
        "var_name": "niteroi_pev",
    },
    {
        "filename": "sao_goncalo_ecopontos_rio_v2.xlsx",
        "section":  "Ecopontos de São Gonçalo",
        "var_name": "sao_goncalo_ecopontos",
    },
    # ── Adicione novos arquivos aqui ──────────────────────────────────────────
    # {
    #     "filename": "nova_iguacu_ecopontos_rio_v2.xlsx",
    #     "section":  "Ecopontos de Nova Iguaçu",
    #     "var_name": "nova_iguacu_ecopontos",
    # },
]

# ── IDs v1 a remover (--delete-old) ──────────────────────────────────────────
# IDs do schema antigo que devem ser apagados antes de enviar os novos.
# Acrescente aqui qualquer ID obsoleto que ainda possa existir no banco.
# Após rodar com --delete-old e confirmar que foi limpo, pode esvaziar esta lista.

TO_DELETE = [
    # Schema v0 (sem prefixo de município)
    "pev_bangu", "pev_madureira", "pev_tijuca",
    "pev_dende", "pev_campo_grande", "pev_marechal_hermes",
    "ecoponto_arara", "ecoponto_barreira_do_vasco", "ecoponto_fogueteiro",
    "ecoponto_jupara", "ecoponto_mangueira", "ecoponto_mineira",
    "ecoponto_providencia", "ecoponto_sao_carlos", "ecoponto_vila_dos_sonhos",
    "ecoponto_acari", "ecoponto_agua_de_ouro", "ecoponto_alemao",
    "ecoponto_av_dom_helder_camara_suipa", "ecoponto_boogie_woogie",
    "ecoponto_camarista_meier", "ecoponto_coelho_neto",
    "ecoponto_comunidade_do_guarda", "ecoponto_costa_barros",
    "ecoponto_fazendinha_campo_do_seu_ze", "ecoponto_fazendinha_praca_da_paloma",
    "ecoponto_guarabu", "ecoponto_juramento", "ecoponto_kelson",
    "ecoponto_madureira", "ecoponto_merindiba", "ecoponto_paim_pamplona",
    "ecoponto_pantoja", "ecoponto_para_pedro", "ecoponto_parada_de_lucas",
    "ecoponto_paranapua", "ecoponto_parque_proletario", "ecoponto_parque_royal",
    "ecoponto_pedreira", "ecoponto_piscinao_de_ramos", "ecoponto_pixunas",
    "ecoponto_praca_do_amarelinho", "ecoponto_praia_da_rosa", "ecoponto_predinhos",
    "ecoponto_quatro_bicas", "ecoponto_querosene",
    "ecoponto_sargento_silvio_hollenbach", "ecoponto_thomas_coelho",
    "ecoponto_tribo", "ecoponto_vigario_geral", "ecoponto_vila_joaniza",
    "ecoponto_campo_grande_viaduto", "ecoponto_campo_grande_rua",
    "ecoponto_catiri", "ecoponto_cinco_marias", "ecoponto_estrada_de_urucania",
    "ecoponto_jacques_ouriques", "ecoponto_jardim_novo",
    "ecoponto_morar_carioca_do_aco", "ecoponto_pedra_de_guaratiba",
    "ecoponto_petrarca", "ecoponto_realengo", "ecoponto_sao_tome",
    "ecoponto_vagao", "ecoponto_vila_alianca", "ecoponto_vila_jurema",
    "ecoponto_vila_kennedy", "ecoponto_vila_vintem",
    "ecoponto_areal", "ecoponto_ayrton_senna", "ecoponto_beira_rio_pantanal",
    "ecoponto_beira_rio_servidao_g7", "ecoponto_cdd_karate", "ecoponto_chacrinha",
    "ecoponto_cidade_de_deus", "ecoponto_conjunto_habitacional_bandeirantes",
    "ecoponto_curva_do_pinheiro", "ecoponto_gilka_machado", "ecoponto_mont_serrat",
    "ecoponto_muzema", "ecoponto_nova_esperanca", "ecoponto_novo_lar",
    "ecoponto_pinheiro", "ecoponto_rio_das_pedras", "ecoponto_santa_maria",
    "ecoponto_sertao", "ecoponto_teixeira_brandao", "ecoponto_terreirao",
    "ecoponto_via_light", "ecoponto_199", "ecoponto_biquinha_vidigal",
    "ecoponto_borda_do_mato", "ecoponto_chacara_do_ceu_vidigal",
    "ecoponto_chacara_do_ceu_borel", "ecoponto_cruzeiro_do_sul",
    "ecoponto_mata_machado", "ecoponto_morro_do_cruz", "ecoponto_nova_divineia",
    "ecoponto_pavao_pavaozinho", "ecoponto_pedro_americo_santo_amaro",
    "ecoponto_pedro_americo_715", "ecoponto_pereira_da_silva",
    "ecoponto_recanto_do_trovador", "ecoponto_rocinha_pastor_almir",
    "ecoponto_roupa_suja", "ecoponto_rua_1", "ecoponto_sa_viana",
    "ecoponto_salgueiro_i", "ecoponto_salgueiro_ii", "ecoponto_santo_amaro",
    "ecoponto_sistema_lagunar", "ecoponto_tavares_bastos", "ecoponto_turano",
    "ecoponto_umuarama", "ecoponto_valao", "ecoponto_vila_verde",
    "ecoponto_vitoria_regia",
    "light_espaco_ciencia_viva", "light_shopping_carioca", "light_humaita",
    "light_plano_inclinado", "light_sao_carlos", "light_assai_duque_de_caxias",
    "light_assai_ilha_do_governador", "light_lar_frei_luiz",
    # Schema v1.5 (underscore, com prefixo de município)
    "pev_comlurb_bangu", "pev_comlurb_madureira", "pev_comlurb_tijuca",
    "pev_comlurb_dende", "pev_comlurb_campo_grande", "pev_comlurb_marechal_hermes",
    "ecoponto_comlurb_arara", "ecoponto_comlurb_barreira_do_vasco",
    "ecoponto_comlurb_fogueteiro", "ecoponto_comlurb_jupara",
    "ecoponto_comlurb_mangueira", "ecoponto_comlurb_mineira",
    "ecoponto_comlurb_providencia", "ecoponto_comlurb_sao_carlos",
    "ecoponto_comlurb_vila_dos_sonhos",
]

# ── Helpers ───────────────────────────────────────────────────────────────────

NOW = datetime.now(timezone.utc).isoformat()


def clean(value) -> str:
    """Converte qualquer valor para string limpa, tratando NaN como vazio."""
    if value is None:
        return ""
    if isinstance(value, float) and str(value) == "nan":
        return ""
    return str(value).strip()


def parse_list(raw: str) -> list:
    """Converte string separada por vírgula em lista, ignorando itens vazios."""
    return [item.strip() for item in raw.split(",") if item.strip()]


def build_address(street: str, number: str, neighborhood: str) -> str:
    """
    Monta o endereço formatado para exibição no app.
    Formato: "Rua X, 123 — Bairro" ou "Rua X, s/n — Bairro"
    O número s/n é incluído explicitamente para deixar claro que não há número.
    """
    parts = [street]
    if number:
        parts.append(number)
    result = ", ".join(parts)
    if neighborhood:
        result += f" — {neighborhood}"
    return result


def load_excel(filepath: str) -> list:
    """
    Lê um arquivo Excel do projeto e retorna lista de dicionários com os dados.
    Espera cabeçalho na linha 2 (header=1 no pandas) e dados a partir da linha 3.
    Ignora linhas onde o campo 'id' está vazio (linhas em branco no fim da planilha).
    """
    df = pd.read_excel(filepath, sheet_name=0, header=1)
    points = []
    for _, row in df.iterrows():
        point_id = clean(row.get("id", ""))
        if not point_id:
            continue  # pula linhas em branco

        points.append({
            "id":               point_id,
            "name":             clean(row.get("name", "")),
            "subtitle":         clean(row.get("subtitle", "")),
            "type":             clean(row.get("type", "")),
            "street":           clean(row.get("street", "")),
            "number":           clean(row.get("number", "")),
            "neighborhood":     clean(row.get("neighborhood", "")),
            "postal_code":      clean(row.get("postal_code", "")),
            "reference":        clean(row.get("reference", "")),
            "city":             clean(row.get("city", "")),
            "state":            clean(row.get("state", "")),
            "latitude":         float(row.get("latitude", 0) or 0),
            "longitude":        float(row.get("longitude", 0) or 0),
            "materials":        parse_list(clean(row.get("materials", ""))),
            "schedule_weekdays":clean(row.get("schedule_weekdays", "")),
            "schedule_saturday":clean(row.get("schedule_saturday", "")),
            "schedule_sunday":  clean(row.get("schedule_sunday", "")),
            "benefits_program": clean(row.get("benefits_program", "")),
            "benefits":         parse_list(clean(row.get("benefits", ""))),
            "active":           bool(row.get("active", True)),
        })
    return points


def build_firestore_payload(point: dict) -> dict:
    """
    Converte um dicionário de ponto (lido do Excel) para o payload do Firestore.

    Schema v2 — estrutura nested:
      address{}     → street, number, neighborhood, city, state
      coordinates{} → latitude, longitude
      schedule{}    → weekdays, saturday, sunday

    Campos diretos: name, subtitle, type, reference, materials,
                    benefitsProgram, benefits, active, createdAt, updatedAt.

    postal_code é armazenado apenas para rastreabilidade interna e não é
    exibido no app — por isso vai como campo direto e não dentro de address{}.
    """
    return {
        "name":     point["name"],
        "subtitle": point["subtitle"],
        "type":     point["type"],
        "address": {
            "street":       point["street"],
            "number":       point["number"],
            "neighborhood": point["neighborhood"],
            "city":         point["city"],
            "state":        point["state"],
        },
        "postal_code": point["postal_code"],  # rastreabilidade, não exibido no app
        "reference":   point["reference"],
        "coordinates": {
            "latitude":  point["latitude"],
            "longitude": point["longitude"],
        },
        "materials": point["materials"],
        "schedule": {
            "weekdays": point["schedule_weekdays"],
            "saturday": point["schedule_saturday"],
            "sunday":   point["schedule_sunday"],
        },
        "benefitsProgram": point["benefits_program"],
        "benefits":        point["benefits"],
        "active":          point["active"],
        "createdAt":       NOW,
        "updatedAt":       NOW,
    }


# ── Firestore ─────────────────────────────────────────────────────────────────

def delete_old_ids(db, ids: list):
    """
    Apaga os IDs do schema antigo em lotes de 400.
    Útil para limpar o banco antes de uma migração de schema.
    Após confirmar a limpeza, esvazie a lista TO_DELETE no topo do script.
    """
    col = db.collection(COLLECTION)
    deleted = 0
    for i in range(0, len(ids), 400):
        batch = db.batch()
        for doc_id in ids[i:i + 400]:
            batch.delete(col.document(doc_id))
        batch.commit()
        deleted += len(ids[i:i + 400])
    print(f"  ✓ {deleted} IDs antigos deletados")


def upload_points(db, points: list):
    """
    Envia os pontos ao Firestore em lotes de 400 (limite: 500 por batch).
    Usa set() sem merge — reexecutar sobrescreve sem duplicar.
    """
    col = db.collection(COLLECTION)
    total = 0
    for i in range(0, len(points), 400):
        batch = db.batch()
        chunk = points[i:i + 400]
        for p in chunk:
            doc_ref = col.document(p["id"])
            batch.set(doc_ref, build_firestore_payload(p))
        batch.commit()
        total += len(chunk)
        print(f"  ✓ {total}/{len(points)} pontos enviados")


def update_metadata(db, total: int):
    """
    Atualiza o documento de metadata usado pelo FirestorePointsSource.kt
    para detectar mudanças sem buscar a coleção inteira.

    O app compara o campo last_updated com o timestamp salvo localmente —
    se forem diferentes, busca os pontos novamente. Por isso é fundamental
    sempre chamar update_metadata após qualquer alteração nos pontos.
    """
    db.collection("metadata").document("recycling_points").set({
        "last_updated":   NOW,
        "schema_version": 2,
        "total_points":   total,
    })
    print(f"  ✓ Metadata atualizado (last_updated: {NOW})")


# ── Geração do fallback Kotlin ────────────────────────────────────────────────

def kt_str(v: str) -> str:
    """Formata uma string como literal Kotlin com aspas duplas."""
    return f'"{v}"'


def kt_list(lst: list) -> str:
    """Formata uma lista de strings como literal Kotlin."""
    if not lst:
        return "emptyList()"
    items = ", ".join(f'"{x}"' for x in lst)
    return f"listOf({items})"


def generate_kotlin_fallback(all_files_data: list) -> str:
    """
    Gera o conteúdo do arquivo RecyclingPointsData.kt a partir dos dados
    lidos dos Excel. Este arquivo é o fallback estático do app Android,
    usado apenas quando o Firestore está inacessível E o cache local está vazio.

    Estrutura gerada:
      - Uma variável privada listOf(...) por arquivo Excel
      - Um val ALL que concatena todas as listas
      - Campos opcionais são omitidos quando vazios (sem "" desnecessário)

    all_files_data: lista de (file_config, points) para cada arquivo Excel.
    """
    lines = []
    var_names = []

    for file_cfg, points in all_files_data:
        section  = file_cfg["section"]
        var_name = file_cfg["var_name"]
        var_names.append(var_name)

        separator = "─" * max(1, 54 - len(section))
        lines.append(f"\n    // ── {section} {separator}")
        lines.append(f"    private val {var_name} = listOf(")

        for p in points:
            address = build_address(p["street"], p["number"], p["neighborhood"])
            lines.append("        RecyclingPoint(")
            lines.append(f"            id               = {kt_str(p['id'])},")
            lines.append(f"            name             = {kt_str(p['name'])},")
            if p["subtitle"]:
                lines.append(f"            subtitle         = {kt_str(p['subtitle'])},")
            lines.append(f"            address          = {kt_str(address)},")
            if p["reference"]:
                lines.append(f"            reference        = {kt_str(p['reference'])},")
            lines.append(f"            latitude         = {p['latitude']},")
            lines.append(f"            longitude        = {p['longitude']},")
            lines.append(f"            materials        = {kt_list(p['materials'])},")
            lines.append(f"            type             = PointType.{p['type']},")
            if p["schedule_weekdays"]:
                lines.append(f"            scheduleWeekdays = {kt_str(p['schedule_weekdays'])},")
            if p["schedule_saturday"]:
                lines.append(f"            scheduleSaturday = {kt_str(p['schedule_saturday'])},")
            if p["schedule_sunday"]:
                lines.append(f"            scheduleSunday   = {kt_str(p['schedule_sunday'])},")
            if p["benefits_program"]:
                lines.append(f"            benefitsProgram  = {kt_str(p['benefits_program'])},")
            if p["benefits"]:
                lines.append(f"            benefits         = {kt_list(p['benefits'])},")
            lines.append("        ),")

        lines.append("    )")

    all_concat = " +\n        ".join(var_names)
    body = "\n".join(lines)

    total = sum(len(pts) for _, pts in all_files_data)
    file_list = "\n".join(f" *   - {fc['filename']}" for fc, _ in all_files_data)

    return f"""package {KT_PACKAGE}

import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint

/**
 * Lista estática de pontos de coleta verificados manualmente.
 *
 * NÃO EDITE ESTE ARQUIVO MANUALMENTE.
 * Ele é gerado automaticamente pelo script populate_firestore_v2.py
 * com a flag --gen-fallback. Para atualizar, edite os arquivos Excel
 * e rode: python populate_firestore_v2.py --gen-fallback
 *
 * QUANDO ESTE ARQUIVO É USADO:
 * O app Android busca pontos de coleta em três camadas:
 *   1. Firestore       → fonte primária (atualizada em runtime)
 *   2. last-known      → cache local (SharedPreferences), atualizado
 *                        automaticamente quando o Firestore responde
 *   3. Este arquivo    → fallback estático compilado no APK, usado
 *                        APENAS no primeiro acesso sem nenhuma internet
 *
 * Qualquer usuário que já abriu o app com internet ao menos uma vez
 * nunca chegará a usar este fallback — o last-known já estará atualizado.
 * Este arquivo garante uma experiência mínima para instalações offline.
 *
 * Gerado em: {NOW}
 * Total de pontos: {total}
 * Arquivos fonte:
{file_list}
 */
internal object RecyclingPointsData {{
{body}

    val ALL: List<RecyclingPoint> = {all_concat}
}}
"""


# ── Main ──────────────────────────────────────────────────────────────────────

def main():
    print("RecycleApp — Populate Firestore v2")
    print(f"Flags: dry-run={DRY_RUN}, delete-old={DELETE_OLD}, gen-fallback={GEN_FALLBACK}")
    print()

    # ── 1. Lê todos os arquivos Excel ─────────────────────────────────────────
    all_files_data = []  # lista de (file_config, points)
    total_points = 0

    for file_cfg in EXCEL_FILES:
        filepath = os.path.join(EXCEL_DIR, file_cfg["filename"])
        if not os.path.exists(filepath):
            print(f"  ⚠ Arquivo não encontrado, ignorando: {filepath}")
            continue
        points = load_excel(filepath)
        all_files_data.append((file_cfg, points))
        total_points += len(points)
        print(f"  ✓ {file_cfg['filename']}: {len(points)} pontos")

    print(f"\nTotal: {total_points} pontos em {len(all_files_data)} arquivos\n")

    if total_points == 0:
        print("Nenhum ponto encontrado. Verifique os arquivos Excel.")
        return

    # ── 2. Gera fallback Kotlin (--gen-fallback) ──────────────────────────────
    # Feito antes do Firestore para funcionar mesmo em --dry-run.
    if GEN_FALLBACK:
        print("Gerando RecyclingPointsData.kt...")
        kt_content = generate_kotlin_fallback(all_files_data)
        output_path = os.path.join(EXCEL_DIR, OUTPUT_KT)
        with open(output_path, "w", encoding="utf-8") as f:
            f.write(kt_content)
        print(f"  ✓ Arquivo gerado: {output_path}")
        print(f"  → Copie para: app/src/main/java/{KT_PACKAGE.replace('.', '/')}/")
        print()

    # ── 3. Dry-run: mostra exemplo e encerra ──────────────────────────────────
    if DRY_RUN:
        import json
        print("[DRY RUN] Exemplo do primeiro ponto de cada arquivo:\n")
        for file_cfg, points in all_files_data:
            if points:
                p = points[0]
                payload = build_firestore_payload(p)
                payload["_id"] = p["id"]
                print(f"--- {file_cfg['filename']} ---")
                print(json.dumps(payload, ensure_ascii=False, indent=2))
                print()
        print("[DRY RUN] Nenhum dado foi enviado ao Firestore.")
        return

    # ── 4. Inicializa Firebase ────────────────────────────────────────────────
    import firebase_admin
    from firebase_admin import credentials, firestore as fs

    key_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "serviceAccountKey.json")
    if not os.path.exists(key_path):
        print(f"Erro: serviceAccountKey.json não encontrado em {EXCEL_DIR}")
        print("Gere a chave em: Firebase Console → Configurações → Contas de serviço")
        return

    cred = credentials.Certificate(key_path)
    firebase_admin.initialize_app(cred)
    db = fs.client()

    # ── 5. Apaga IDs antigos (--delete-old) ───────────────────────────────────
    if DELETE_OLD and TO_DELETE:
        print(f"Apagando {len(TO_DELETE)} IDs do schema antigo...")
        delete_old_ids(db, TO_DELETE)
        print()

    # ── 6. Envia pontos ao Firestore ──────────────────────────────────────────
    all_points = [p for _, pts in all_files_data for p in pts]
    print(f"Enviando {len(all_points)} pontos ao Firestore...")
    upload_points(db, all_points)

    # ── 7. Atualiza metadata ──────────────────────────────────────────────────
    # Fundamental: o app usa last_updated para detectar mudanças.
    # Sem isso, os usuários não receberão os dados novos.
    print("\nAtualizando metadata...")
    update_metadata(db, len(all_points))

    print(f"\n✓ Concluído. {len(all_points)} pontos no Firestore.")


if __name__ == "__main__":
    main()
