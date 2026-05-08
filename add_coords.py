import re
import xml.etree.ElementTree as ET

net_file = r'c:\Users\datan\IdeaProjects\matsim-code-examples\scenarios\berlin_drt\berlin-mitte_network.xml'
net_tree = ET.parse(net_file)
nodes = {n.get('id'): (n.get('x'), n.get('y')) for n in net_tree.findall('.//node')}
link_coords = {}
for l in net_tree.findall('.//link'):
    coord = nodes.get(l.get('to'))
    if coord:
        link_coords[l.get('id')] = coord

def process_plans(plans_file):
    with open(plans_file, 'r', encoding='utf-8') as f:
        content = f.read()
        
    def repl(m):
        link_id = m.group(1)
        if link_id in link_coords:
            x, y = link_coords[link_id]
            # Don't add if already there
            if f' x="{x}"' not in m.group(0):
                return f'link="{link_id}" x="{x}" y="{y}"'
        return m.group(0)
        
    new_content = re.sub(r'link="([^"]+)"', repl, content)
    with open(plans_file, 'w', encoding='utf-8') as f:
        f.write(new_content)
        print(f"Processed {plans_file}")

process_plans(r'c:\Users\datan\IdeaProjects\matsim-code-examples\scenarios\berlin_drt\berlin-mitte_drt_plans.xml')
process_plans(r'c:\Users\datan\IdeaProjects\matsim-code-examples\scenarios\berlin_drt\berlin-mitte_drt_10_passengers_plans.xml')
