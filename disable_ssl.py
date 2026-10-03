import os
import glob
for path in glob.glob('c:/Wefit/**/*.yml', recursive=True):
    with open(path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    changed = False
    in_ssl = False
    indent = 0
    new_lines = []
    for line in lines:
        stripped = line.lstrip()
        if stripped.startswith('ssl:'):
            in_ssl = True
            indent = len(line) - len(stripped)
            new_lines.append(line.replace('ssl:', '# ssl:'))
            changed = True
            continue
        if in_ssl:
            curr_indent = len(line) - len(stripped)
            if curr_indent > indent and stripped != '':
                new_lines.append('# ' + line)
                changed = True
                continue
            else:
                in_ssl = False
        if stripped.startswith('defaultZone:') and 'https' in line:
            new_lines.append(line.replace('https', 'http'))
            changed = True
            continue
        new_lines.append(line)
    if changed:
        with open(path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print('Modified', path)
