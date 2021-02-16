import sys
import os
sys.path.append(os.getcwd())
from modules.person_counter_opencv.people_counter import main, counter_test

def test_module_import():
    assert counter_test() == 1