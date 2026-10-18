"""Prints the centre of a labelled node in a uiautomator dump read from stdin.

python tools/labels.py <key> en|ru < ui.xml
"""
import re
import sys

LABELS = {
    "nana": {"en": "Nana Rosa", "ru": "Бабушка Нина"},
    "loteria": {"en": "Lotería of fears", "ru": "Лотерея страхов"},
    "draw": {"en": "Draw today", "ru": "Вытянуть карту дня"},
    "calaveritas": {"en": "Calaveritas", "ru": "Калаверитас"},
    "picado": {"en": "Papel picado", "ru": "Папель пикадо"},
    "unfold": {"en": "Unfold", "ru": "Развернуть"},
}

key, lang = sys.argv[1], sys.argv[2]
want = LABELS[key][lang]
x = sys.stdin.buffer.read().decode("utf-8")
nodes = re.findall(r"<node [^>]*>", x)
hits = [n for n in nodes if f'text="{want}' in n]
if hits:
    a = list(map(int, re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', hits[0]).groups()))
    print((a[0] + a[2]) // 2, (a[1] + a[3]) // 2)
