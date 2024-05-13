<?php

const INI_FILE_PATH = 'demo.ini';
const LOCALHOST_IP = '127.0.0.1';
const URL_ARGUMENT_KEY = 'demo_problem';

$demoProblems = parse_ini_file(INI_FILE_PATH, true);
$demoProblemsNames = array_keys($demoProblems);

$demoProblemsLinks = join(array_map(function ($demoProblemName) {
    $argumentKey = URL_ARGUMENT_KEY;
    return "<a href=\"?$argumentKey=$demoProblemName\">$demoProblemName</a> ";
}, $demoProblemsNames));

$input = "description = ''\nlogic = 'PropositionalLogic'\nvars = 'P'\nconclusion = ''";

$demoProblemName = $_GET[URL_ARGUMENT_KEY];
if (isset($demoProblemName) && in_array($demoProblemName, $demoProblemsNames))
{
    $demoProblem = $demoProblems[$demoProblemName];
    $demoProblem['debug'] = $_SERVER['REMOTE_ADDR'] == LOCALHOST_IP ? 'true' : 'false';

    ob_start();
    foreach ($demoProblem as $key => $value)
        echo "$key = '$value'\n";
    $input = ob_get_contents();
    ob_end_clean();
}

echo <<<EOHTML
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Graham Priest deduction</title>
</head>
<body>
    Demo: $demoProblemsLinks
    <textarea id="inputTextArea" style="width: 100%; height: 15em;">$input</textarea>
    <button id="proveButton">PROVE!</button>
    <pre id="resultArea"></pre>
    <script src="target-js.js"></script>
</body>
</html>
EOHTML;
