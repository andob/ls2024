<?php

//todo de implementat toate problemele din carte
const INI_FILE_PATH = 'demo.ini';
const LOCALHOST_IP = '127.0.0.1';
const PROBLEM_ARGUMENT_KEY = 'demo_problem';
const DEBUG_ARGUMENT_KEY = 'debug';

$debugMode = isset($_GET[DEBUG_ARGUMENT_KEY]) ? $_GET[DEBUG_ARGUMENT_KEY] : $_SERVER['REMOTE_ADDR'] == LOCALHOST_IP;

$demoProblems = parse_ini_file(INI_FILE_PATH, true);
$demoProblemsNames = array_keys($demoProblems);

$demoProblemsLinks = join(array_map(function ($demoProblemName) use ($debugMode) {
    $problemKey = PROBLEM_ARGUMENT_KEY;
    $debugModeKey = DEBUG_ARGUMENT_KEY;
    return "<a href=\"?$problemKey=$demoProblemName&$debugModeKey=$debugMode\">$demoProblemName</a> ";
}, $demoProblemsNames));

$input = "description = ''\nlogic = 'PropositionalLogic'\nvars = 'P'\nconclusion = ''";

$demoProblemName = isset($_GET[PROBLEM_ARGUMENT_KEY]) ? $_GET[PROBLEM_ARGUMENT_KEY] : null;
if (isset($demoProblemName) && in_array($demoProblemName, $demoProblemsNames))
{
    $demoProblem = $demoProblems[$demoProblemName];

    ob_start();
    foreach ($demoProblem as $key => $value)
        echo "$key = '$value'\n";
    $input = ob_get_contents();
    ob_end_clean();
}

$resultTreeAreaStyle = $debugMode ? 'display: block' : 'display: none';

echo <<<EOHTML
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tableaux</title>
</head>
<body>

    <div style="float: left; width: 50%;">
        <div>Demo: $demoProblemsLinks</div>
        <textarea id="inputTextArea" style="width: 100%; height: 15em;">$input</textarea>
        <div id="onScreenKeyboard"></div>
        <button id="proveButton">PROVE!</button>
        <div id="resultTextArea"></div>
        <pre id="resultTreeArea" style="$resultTreeAreaStyle"></pre>
    </div>
    
    <div style="float: left; width: 50%; height: 100vh">
        <div id="resultPrettyTreeArea" style="width: 100%; height: 100%; display: block"></div>
    </div>
    
    <script src="target-js.js"></script>
    <script src="keyboard.js"></script>
    
    <script src="https://cdn.jsdelivr.net/npm/dagre@0.8.5/dist/dagre.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/cytoscape@3.29.2/dist/cytoscape.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/cytoscape-dagre@2.5.0/cytoscape-dagre.min.js"></script>
    <script>function refreshPrettyTreeUI(contents) { window.refreshPrettyTreeUIImpl(contents); }</script>
    <script src="tree-prettifier.js"></script>
    
</body>
</html>
EOHTML;
