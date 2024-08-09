package utb.fai.Exception;

import utb.fai.Core.StatusCode;

public class NonUniqueModuleNamesException extends Exception {

    public NonUniqueModuleNamesException(String duplicitName) {
        super("The names of modules are not unique. Name [" + duplicitName + "] is found in multiple elements.");
    }

    public int getErrorCode() {
        return StatusCode.NON_UNIQUE_MODULE_NAMES;
    }

}