

def merge(filename):
    for l in file(filename):
        if l.startswith("@import"):
            merged_filename = l.split('"')[1]
            merge(merged_filename)
        else:
            print l,

if __name__ == '__main__':
    merge("bootstrap.less")
