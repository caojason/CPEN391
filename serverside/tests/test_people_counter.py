import sys
import os
sys.path.append(os.getcwd())
from modules.Person_Counter_Opencv.People_Counter import main, counter_test

def test_module_import():
    assert counter_test() == 1