"""Draws the launcher icon (a painted sugar skull on the night sky, crowned with marigolds) and docs/logo.png.

python tools/icon.py
"""
import math
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"
NIGHT = (42, 31, 74)
BONE = (246, 238, 221)
EDGE = (190, 176, 150)
SOCKET = (26, 19, 48)
FUCHSIA = (232, 65, 142)
TURQUOISE = (38, 198, 179)
MARIGOLD = (247, 162, 27)
DEEP = (217, 103, 11)
GOLD = (245, 200, 76)
LEAF = (63, 163, 77)
S = 8  # supersampling


def skull(size: int) -> Image.Image:
    """The skull alone, transparent around it, on a size x size canvas laid out in 108 dp units."""
    W = size * S
    im = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    u = W / 108
    cx, cy = W / 2, W / 2 + 3 * u

    def ell(x, y, rx, ry, fill):
        d.ellipse((x - rx, y - ry, x + rx, y + ry), fill=fill)

    # Jaw, then the cranium over it.
    d.rounded_rectangle((cx - 13 * u, cy + 4 * u, cx + 13 * u, cy + 22 * u), radius=7 * u, fill=EDGE)
    d.rounded_rectangle((cx - 12 * u, cy + 4 * u, cx + 12 * u, cy + 21 * u), radius=6.5 * u, fill=BONE)
    ell(cx, cy - 4 * u, 21.5 * u, 20.5 * u, EDGE)
    ell(cx, cy - 4 * u, 20.5 * u, 19.5 * u, BONE)
    # Petals round the eyes, alternating colours.
    for ex in (-9.6, 9.6):
        ox, oy = cx + ex * u, cy + 1 * u
        for k in range(10):
            a = k * math.pi / 5
            # Leave the bridge of the nose bare, where the two rings would meet.
            if (ex < 0 and k in (0, 1, 9)) or (ex > 0 and k in (4, 5, 6)):
                continue
            px, py = ox + math.cos(a) * 9 * u, oy + math.sin(a) * 9.6 * u
            ell(px, py, 1.9 * u, 1.9 * u, FUCHSIA if k % 2 == 0 else TURQUOISE)
        ell(ox, oy, 6.6 * u, 7.2 * u, SOCKET)
        ell(ox + 2 * u, oy - 2.2 * u, 1.4 * u, 1.4 * u, (255, 255, 255, 230))
    # An upside-down heart for a nose.
    nx, ny = cx, cy + 10 * u
    d.polygon([(nx - 3.2 * u, ny - 2 * u), (nx + 3.2 * u, ny - 2 * u), (nx, ny + 2.6 * u)], fill=SOCKET)
    ell(nx - 1.6 * u, ny - 2 * u, 1.7 * u, 1.4 * u, SOCKET)
    ell(nx + 1.6 * u, ny - 2 * u, 1.7 * u, 1.4 * u, SOCKET)
    # Teeth.
    ty = cy + 16.5 * u
    d.line((cx - 8 * u, ty, cx + 8 * u, ty), fill=SOCKET, width=int(0.9 * u))
    for k in range(-3, 4):
        x = cx + k * 2.4 * u
        d.line((x, ty - 2.6 * u, x, ty + 3 * u), fill=SOCKET, width=int(0.8 * u))
    # Marigolds across the brow, a big one in the middle.
    for k, (dx, r) in enumerate([(-15, 4.2), (-8, 5), (0, 6.2), (8, 5), (15, 4.2)]):
        a = math.radians(-90 + dx * 3.2)
        fx, fy = cx + math.cos(a) * 19 * u, cy - 4 * u + math.sin(a) * 18.5 * u
        if k in (1, 3):
            ell(fx + (3 if dx > 0 else -3) * u, fy + 3 * u, 2.6 * u, 1.3 * u, LEAF)
        for j in range(12):
            b = j * math.pi / 6
            ell(fx + math.cos(b) * r * 0.62 * u, fy + math.sin(b) * r * 0.62 * u, r * 0.42 * u, r * 0.42 * u, DEEP)
        ell(fx, fy, r * 0.72 * u, r * 0.72 * u, MARIGOLD)
        ell(fx, fy, r * 0.28 * u, r * 0.28 * u, GOLD)
    return im.resize((size, size), Image.LANCZOS)


def tile(size: int, round_: bool) -> Image.Image:
    W = size * S
    bg = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    mask = Image.new("L", (W, W), 0)
    md = ImageDraw.Draw(mask)
    if round_:
        md.ellipse((0, 0, W - 1, W - 1), fill=255)
    else:
        md.rounded_rectangle((0, 0, W - 1, W - 1), radius=int(W * 0.22), fill=255)
    bg.paste(Image.new("RGBA", (W, W), NIGHT + (255,)), (0, 0), mask)
    bg = bg.resize((size, size), Image.LANCZOS)
    fg = skull(int(size * 1.5)).crop((int(size * 0.25), int(size * 0.25), int(size * 1.25), int(size * 1.25)))
    bg.alpha_composite(fg)
    return bg


def main():
    for name, dp in {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}.items():
        folder = RES / f"mipmap-{name}"
        folder.mkdir(parents=True, exist_ok=True)
        skull(int(108 * dp)).save(folder / "ic_launcher_foreground.png")
        tile(int(48 * dp), False).save(folder / "ic_launcher.png")
        tile(int(48 * dp), True).save(folder / "ic_launcher_round.png")
    (ROOT / "docs").mkdir(exist_ok=True)
    tile(512, False).save(ROOT / "docs/logo.png")


if __name__ == "__main__":
    main()
