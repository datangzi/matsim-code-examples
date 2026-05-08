import xml.etree.ElementTree as ET
import matplotlib.pyplot as plt
from matplotlib.collections import LineCollection
import argparse
import sys
import os

def visualize_network(network_file, output_file=None):
    if not os.path.exists(network_file):
        print(f"Error: File '{network_file}' does not exist.")
        sys.exit(1)
        
    print(f"Parsing {network_file}...")
    
    nodes = {} # id -> (x, y)
    links = [] # list of ((x1, y1), (x2, y2))
    
    # Use iterparse for memory efficiency and speed
    context = ET.iterparse(network_file, events=('end',))
    
    for event, elem in context:
        if elem.tag == 'node':
            node_id = elem.attrib['id']
            x = float(elem.attrib['x'])
            y = float(elem.attrib['y'])
            nodes[node_id] = (x, y)
            elem.clear() # clear memory
        elif elem.tag == 'link':
            from_node = elem.attrib['from']
            to_node = elem.attrib['to']
            if from_node in nodes and to_node in nodes:
                links.append((nodes[from_node], nodes[to_node]))
            elem.clear()
            
    print(f"Parsed {len(nodes)} nodes and {len(links)} links.")
    
    print("Plotting network...")
    fig, ax = plt.subplots(figsize=(12, 12))
    
    # Using LineCollection for much faster plotting of many lines
    lc = LineCollection(links, color='blue', linewidths=0.2, alpha=0.5)
    ax.add_collection(lc)
    
    ax.autoscale()
    ax.set_aspect('equal')
    ax.set_title(f"MATSim Network: {os.path.basename(network_file)}")
    ax.set_xlabel("X coordinate")
    ax.set_ylabel("Y coordinate")
    
    # Hide spines for a cleaner look
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    if output_file:
        print(f"Saving visualization to {output_file}...")
        plt.savefig(output_file, dpi=300, bbox_inches='tight')
        print("Done!")
    else:
        print("Showing plot...")
        plt.show()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Visualize a MATSim network XML file.")
    parser.add_argument("network_file", nargs='?', default="berlin-mitte_network.xml", help="Path to the network.xml file")
    parser.add_argument("-o", "--output", help="Path to save the output image (optional)")
    args = parser.parse_args()
    
    visualize_network(args.network_file, args.output)
