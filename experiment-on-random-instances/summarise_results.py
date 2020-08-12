import os

filenames = os.listdir("runtimes")

def parsetime(s):
    t = s.split("m")
    t[1] = t[1][:-1]
    return int(t[0]) * 60 + float(t[1])

results = {}

for name in filenames:
    tokens = name.split("-")
    if tokens[4] != "n.time":
        continue
    n = int(tokens[1])
    p = int(tokens[2])
    it = int(tokens[3])

    if n not in results:
        results[n] = {}
    if p not in results[n]:
        results[n][p]= {}
    for program in ["n", "n2", "o"]:
        if program not in results[n][p]:
            results[n][p][program] = []
        filename = "runtimes/gr-{}-{}-{}-{}.time".format(n, p, it, program)
        with open(filename, "r") as f:
            lines = [line.strip().split() for line in f.readlines()]
#            print(lines)
            for line in lines:
                if line and line[0] == "real":
                    results[n][p][program].append(parsetime(line[1]))

#print(results)

rows = []
for n, ps in sorted(results.items()):
    for p, progs in sorted(ps.items()):
        row = [n, p]
        for prog in ["o", "n", "n2"]:
            row.append(sum(progs[prog]) / len(progs[prog]))
        rows.append(row)

for row in sorted(rows, key=lambda row: (row[1], row[0])):
    print("{} {} {:.2f} {:.2f} {:.2f}".format(*row))
