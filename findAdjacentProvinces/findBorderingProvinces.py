#!/usr/bin/env python3
import csv
import fcntl
import os
import pickle
import time
from multiprocessing.pool import Pool
from typing import List, Dict

from PIL import Image


class Color:
    def __init__(self, r: int, g: int, b: int):
        self.r: int = r
        self.g: int = g
        self.b: int = b

    def matches(self, r: int, g: int, b: int) -> bool:
        return self.r == r and self.g == g and self.b == b

    def __eq__(self, other) -> bool:
        return (self.r, self.g, self.b) == (other.r, other.g, other.b)

    def __hash__(self) -> int:
        return hash((self.r, self.g, self.b))

    def __str__(self) -> str:
        return '(%d, %d, %d)' % (self.r, self.g, self.b)


class Province:
    def __init__(self, province_id: int, r: int, g: int, b: int, name: str, *args):
        self.id: int = int(province_id)
        self.color: Color = Color(int(r), int(g), int(b))
        self.name = name
        self.pixels: List[Coord] = []
        self.adjacent: List[Province] = []
        self.coastal = False

    def populate_adjacent_provinces(self, province_list: List['Province']):
        sea_provinces = get_sea_province_ids()
        print('Finding adjacent provinces for %s' % self.name)
        prev_time = time.time()
        for check_province in province_list:
            if self == check_province or check_province in self.adjacent:
                continue

            if self in check_province.adjacent or self.is_adjacent(check_province):
                if check_province.id in sea_provinces:
                    self.coastal = True
                    print(' - %s(%d) is coastal' % (self.name, self.id))
                else:
                    self.adjacent.append(check_province)
                    print(' - %s(%d) is adjacent to %s(%d)' % (self.name, self.id, check_province.name, check_province.id))

        print('Found adjacent for %s in %ds' % (self.name, (time.time() - prev_time)))
        with open('adjacencies.txt', 'a') as f:
            fcntl.flock(f, fcntl.LOCK_EX)
            f.write("%s=%s=%s\n" % (self.id, str(self.coastal).lower(), ','.join([str(x.id) for x in self.adjacent])))
            fcntl.flock(f, fcntl.LOCK_UN)

    # A province is adjacent to another province if any pixel in that province is adjacent
    # to a pixel in the other province.
    def is_adjacent(self, check_province: 'Province') -> bool:
        for my_coord in self.pixels:
            for other_coord in check_province.pixels:
                if my_coord.is_adjacent(other_coord):
                    return True

        return False

    def __eq__(self, other):
        return self.id == other.id


class Coord:
    def __init__(self, x: int, y: int):
        self.x: int = x
        self.y: int = y

    def __str__(self):
        return '(%s, %s)' % (self.x, self.y)

    def is_adjacent(self, other: 'Coord') -> bool:
        return (self.x == other.x and abs(self.y - other.y) == 1) or (self.y == other.y and abs(self.x - other.x) == 1)


def save_color_map():
    print('Building color map')
    color_map: Dict[Color, List[Coord]] = {}
    with Image.open('./map/provinces.bmp') as image:
        rgb = image.convert('RGB')
        for x in range(5632):
            for y in range(2048):
                color = Color(*rgb.getpixel((x, y)))
                if 0 < x < 5631 and 0 < y < 2047:
                    adjacent_colors = [
                        Color(*rgb.getpixel((x + 1, y))),
                        Color(*rgb.getpixel((x - 1, y))),
                        Color(*rgb.getpixel((x, y + 1))),
                        Color(*rgb.getpixel((x, y - 1))),
                        Color(*rgb.getpixel((x + 1, y + 1))),
                        Color(*rgb.getpixel((x - 1, y + 1))),
                        Color(*rgb.getpixel((x + 1, y - 1))),
                        Color(*rgb.getpixel((x - 1, y - 1))),
                    ]

                    if len(set(adjacent_colors)) == 1:
                        continue

                if color not in color_map:
                    color_map[color] = []

                color_map[color].append(Coord(x, y))

    with open('color_map.pickle', 'wb') as color_map_file:
        pickle.dump(color_map, color_map_file, protocol=pickle.HIGHEST_PROTOCOL)

    return color_map


def load_color_map():
    print('Loading color map')
    with open('color_map.pickle', 'rb') as color_map_file:
        return pickle.load(color_map_file)


def get_sea_province_ids() -> List[int]:
    sea_provinces: List[int] = list(range(1252, 1260))
    sea_provinces.extend([1652, 1924, 1932, 1975, 1980, 4333, 4346, 4347, 4357, 4358])
    sea_provinces.extend(range(1263, 1273))
    sea_provinces.extend(range(1274, 1306))
    sea_provinces.extend(range(1307, 1318))
    sea_provinces.extend(range(1319, 1325))
    sea_provinces.extend(range(1328, 1648))
    sea_provinces.extend(range(1666, 1742))
    sea_provinces.extend(range(1926, 1930))
    sea_provinces.extend(range(4224, 4227))
    sea_provinces.extend(range(4233, 4236))

    return sea_provinces


def get_skipped_province_ids() -> List[int]:
    provinces_to_skip: List[int] = list(range(3004, 4020))
    provinces_to_skip.extend([1173, 1779, 1810, 1811, 1812, 1814, 1932, 1950, 1975, 1976, 1977, 1980, 2000, 2001, 2129,
                              2194, 2200, 2251, 2334, 2425, 2426, 2471, 2608, 2740, 2936, 2953, 4146, 4172, 4177, 4178,
                              4179, 4224, 4225, 4226, 4233, 4234, 4235, 4276, 4321, 4322, 4328, 4333, 4346, 4347, 4357,
                              4358, 4522])
    provinces_to_skip.extend(range(1250, 1273))
    provinces_to_skip.extend(range(1274, 1306))
    provinces_to_skip.extend(range(1307, 1318))
    provinces_to_skip.extend(range(1319, 1742))
    provinces_to_skip.extend(range(1781, 1792))
    provinces_to_skip.extend(range(1793, 1800))
    provinces_to_skip.extend(range(1801, 1809))
    provinces_to_skip.extend(range(1883, 1901))
    provinces_to_skip.extend(range(1902, 1925))
    provinces_to_skip.extend(range(1926, 1930))
    provinces_to_skip.extend(range(4132, 4141))
    provinces_to_skip.extend(range(4153, 4158))
    provinces_to_skip.extend(range(4159, 4163))
    provinces_to_skip.extend(range(4167, 4171))
    provinces_to_skip.extend(range(4400, 4404))

    if os.path.isfile('adjacencies.txt'):
        with open('adjacencies.txt', 'r') as f:
            for line in f:
                provinces_to_skip.append(int(line.split('=')[0]))

    return provinces_to_skip


color_map = load_color_map() if os.path.isfile('color_map.pickle') else save_color_map()

print('Building province list')
provinces: List[Province] = []
provinces_to_skip: List[int] = get_skipped_province_ids()
sea_provinces: List[int] = get_sea_province_ids()
with open('./map/definition.csv', 'r', encoding='latin_1') as definition:
    reader = csv.reader(definition, delimiter=';')
    next(reader, None)

    for row in reader:
        province_id = int(row[0])
        if province_id in provinces_to_skip and province_id not in sea_provinces:
            print('Skipping province %d' % province_id)
            continue

        provinces.append(Province(*row))

print('Assigning pixels to provinces')
for province in provinces:
    province.pixels = color_map[province.color]

print('Attempting to fill adjacent provinces')
print('Doing adjacencies csv')
with open('./map/adjacencies.csv', 'r', encoding='latin_1') as adjacencies:
    reader = csv.reader(adjacencies, delimiter=';')
    next(reader, None)

    for row in reader:
        prov1 = next((province for province in provinces if province.id == int(row[0])), None)
        prov2 = next((province for province in provinces if province.id == int(row[1])), None)

        if prov1 is None or prov2 is None:
            continue

        print(' - %s(%d) is adjacent to %s(%d)' % (prov1.name, prov1.id, prov2.name, prov2.id))
        prov1.adjacent.append(prov2)
        prov2.adjacent.append(prov1)

pool = Pool()
provinces_to_check = [p for p in provinces if p.id not in provinces_to_skip]
for province in provinces_to_check:
    pool.apply_async(province.populate_adjacent_provinces, args=(provinces,))

pool.close()
pool.join()
