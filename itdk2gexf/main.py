import gexf
import os
from multiprocessing import Pool


def load_geo():
    geos = {}
    with open('data/midar-iff.nodes.geo') as fp:
        for line in fp:
            if line[0] == '#':
                continue
            elem = line[:-1].split('\t')
            name = elem[0][10:-1]
            latitude = elem[5]
            longitude = elem[6]
            geos[name] = (latitude, longitude)
    return geos


def load_as_link(filename, nodes, edges):
    with open('data/' + filename) as fp:
        for line in fp:
            if line[0] == '#' or line[0] == 'M' or line[0] == 'T':
                continue
            elem = line[:-1].split('\t')
            if elem[0] == 'D' or elem[0] == 'I':
                if '.' in line or '_' in line:
                    continue
                source = elem[1]
                target = elem[2]
                nodes.add(source)
                nodes.add(target)
                edges.add('%s-%s' % (source, target))


def build_graph(nodes, edges, geo):
    context = gexf.Gexf('Terro', 'ITDK ASes Data')
    graph = context.addGraph('directed', 'static', 'ITDK ASes Data')
    id_latitude = graph.addNodeAttribute('lat', type='double')
    id_longitude = graph.addNodeAttribute('lon', type='double')

    for node_id in nodes:
        if node_id not in geo:
            continue
        node = graph.addNode(node_id, node_id)
        pos = geo[node_id]
        node.addAttribute(id_latitude, pos[0])
        node.addAttribute(id_longitude, pos[1])

    for edge in edges:
        source, target = edge.split('-')
        if source not in geo or target not in geo:
            continue
        graph.addEdge(edge, source, target)

    return context


def process(filename):
    global geo
    nodes = set()
    edges = set()
    print filename[28:36]
    load_as_link(filename, nodes, edges)
    graph = build_graph(nodes, edges, geo)
    output_file = open('/Users/terro/dataset/' + filename[28:36]+'.gexf', 'w')
    graph.write(output_file)
    output_file.close()


geo = load_geo()
for _, _, files in os.walk("data"):
    p = Pool(8)
    p.map(process, [filename for filename in files if filename.endswith('txt')])
