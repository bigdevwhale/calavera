"""Builds README / Stillcraft screenshots from raw phone captures.

python tools/shots.py <raw_dir> <out_dir>

<raw_dir> holds full-screen captures (1080x2400) named <name>.png and <name>.ru.png (tools/capture.sh).
Each one is cropped below the status bar, scaled to 540 px wide with rounded corners, and four of
them are laid out under a night sky strung with papel picado as overview.png / overview.ru.png.
"""
import random
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

STATUS = 110  # status bar height at 1080 px wide
OVERVIEW = ["casa", "ofrenda", "card", "fiesta"]
SINGLE = ["casa", "ofrenda", "soul", "fiesta", "card", "calaveritas", "picado", "wardrobe"]

NIGHT = (30, 23, 51)
DEEP = (44, 35, 80)
TISSUE = [(232, 65, 142), (38, 198, 179), (247, 162, 27), (185, 216, 60), (155, 107, 234), (255, 143, 184)]


def card(src: Path) -> Image.Image:
    im = Image.open(src).convert("RGB")
    w, h = im.size
    im = im.crop((0, STATUS, w, STATUS + 2290))
    im = im.resize((540, 1145), Image.LANCZOS)
    mask = Image.new("L", im.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, im.size[0] - 1, im.size[1] - 1), 34, fill=255)
    out = Image.new("RGBA", im.size)
    out.paste(im, (0, 0), mask)
    return out


def ground(w: int, h: int) -> Image.Image:
    g = Image.new("RGB", (w, h), NIGHT)
    d = ImageDraw.Draw(g)
    for y in range(h):
        t = (y / h) ** 1.4
        d.line([(0, y), (w, y)], fill=tuple(int(NIGHT[i] * (1 - t) + DEEP[i] * t) for i in range(3)))
    rnd = random.Random(5)
    for _ in range(140):
        x, y, r = rnd.randint(0, w), rnd.randint(0, h), rnd.choice([1, 1, 2, 3])
        d.ellipse((x - r, y - r, x + r, y + r), fill=(251, 241, 222))
    # A string of flags across the top, and petals strewn along the bottom.
    d.line([(0, 18), (w, 18)], fill=(237, 227, 207), width=3)
    fw = 120
    for i, x in enumerate(range(20, w, fw + 40)):
        col = TISSUE[i % len(TISSUE)]
        pts = [(x, 18), (x + fw, 18), (x + fw, 150)] + [(x + fw - k * fw / 6 - (fw / 12 if k % 2 else 0), 150 + (8 if k % 2 else 0)) for k in range(1, 12)] + [(x, 150)]
        d.polygon(pts, fill=col)
        for k in range(5):
            cx, cy = x + 20 + k * 20, 60 + (k % 2) * 30
            d.ellipse((cx - 6, cy - 6, cx + 6, cy + 6), fill=NIGHT)
    for _ in range(220):
        x, y = rnd.randint(0, w), rnd.randint(h - 70, h)
        r = rnd.randint(4, 9)
        d.ellipse((x - r, y - r, x + r, y + r), fill=rnd.choice([(247, 162, 27), (217, 103, 11)]))
    return g


def overview(cards: list[Image.Image]) -> Image.Image:
    W, H = 2400, 1380
    g = ground(W, H).convert("RGBA")
    xs = [64, 646, 1228, 1810]
    ys = [130, 180, 130, 180]
    for c, x, y in zip(cards, xs, ys):
        c = c.crop((0, 0, c.size[0], min(c.size[1], H - y - 40)))
        shadow = Image.new("RGBA", (c.size[0] + 80, c.size[1] + 80), (0, 0, 0, 0))
        ImageDraw.Draw(shadow).rounded_rectangle((40, 40, 40 + c.size[0], 40 + c.size[1]), 34, fill=(0, 0, 0, 150))
        shadow = shadow.filter(ImageFilter.GaussianBlur(20))
        g.alpha_composite(shadow, (x - 40, y - 26))
        g.alpha_composite(c, (x, y))
        frame = Image.new("RGBA", (c.size[0] + 4, c.size[1] + 4), (0, 0, 0, 0))
        ImageDraw.Draw(frame).rounded_rectangle((0, 0, frame.size[0] - 1, frame.size[1] - 1), 36, outline=(247, 162, 27), width=3)
        g.alpha_composite(frame, (x - 2, y - 2))
    return g.convert("RGB")


def main() -> None:
    raw, out = Path(sys.argv[1]), Path(sys.argv[2])
    out.mkdir(parents=True, exist_ok=True)
    for suffix in ["", ".ru"]:
        made = {}
        for name in SINGLE:
            src = raw / f"{name}{suffix}.png"
            if src.exists():
                made[name] = card(src)
                made[name].save(out / f"{name}{suffix}.png", optimize=True)
        if all(n in made for n in OVERVIEW):
            overview([made[n] for n in OVERVIEW]).save(out / f"overview{suffix}.png", optimize=True)


if __name__ == "__main__":
    main()
