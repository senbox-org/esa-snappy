import sys
import sysconfig


def print_site_packages_dir():
    print(sysconfig.get_path('platlib'))


if __name__ == '__main__':
    globals()[sys.argv[1]]()