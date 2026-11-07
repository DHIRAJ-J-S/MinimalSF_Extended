package io.github.dhiraj_j_s.minimalsf.extended;

// Runs in Shizuku's process, with shell (or root) rights
interface IShellService {
    // Shizuku calls this transaction to stop the service
    void destroy() = 16777114;

    // Runs a program with arguments (no shell in between). Returns "<exit code>\n<output>".
    String run(in String[] argv) = 1;

    // Deletes an app's cache folders. Only works when Shizuku runs as root. Returns "<exit code>\n<message>".
    String clearCache(String pkg, int userId) = 2;
}
