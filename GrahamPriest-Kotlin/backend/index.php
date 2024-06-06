<?php

const DEMO_PROBLEMS_INI_FILE_PATH = 'demo.ini';
const CHAPTERS_JSON_FILE_PATH = 'chapters.json';
const PROBLEM_ARGUMENT_KEY = 'demo_problem';

$demoProblems = parse_ini_file(DEMO_PROBLEMS_INI_FILE_PATH, true);
$demoProblemsNames = array_keys($demoProblems);

$bookChapters = json_decode(file_get_contents(CHAPTERS_JSON_FILE_PATH));
$demoProblemsLinks = join(array_map(function ($bookChapter) use ($demoProblemsNames, $bookChapters) {

    $bookChapterNumber = 1 + (int)array_search($bookChapter, $bookChapters);

    $demoProblemsNamesOnChapter = array_filter($demoProblemsNames, fn($demoProblemName) =>
        strpos($demoProblemName, $bookChapterNumber . '.') === 0);

    $problemArgumentKey = PROBLEM_ARGUMENT_KEY;
    $demoProblemsLinks = join(array_map(fn($demoProblemName) =>
        "<a href=\"?$problemArgumentKey=$demoProblemName\">$demoProblemName</a> ", $demoProblemsNamesOnChapter));

    return "<div class=\"accordion\">CH$bookChapterNumber $bookChapter:<br/>$demoProblemsLinks</div>";
}, $bookChapters));

$input = "description = ''\nlogic = 'PropositionalLogic'\nvars = 'P'\nconclusion = 'P ∨ ¬P'";

$demoProblemName = $_GET[PROBLEM_ARGUMENT_KEY] ?? null;
if (isset($demoProblemName) && in_array($demoProblemName, $demoProblemsNames))
{
    $demoProblem = $demoProblems[$demoProblemName];

    ob_start();
    foreach ($demoProblem as $key => $value)
        echo "$key = '$value'\n";
    $input = ob_get_contents();
    ob_end_clean();
}

header('Content-type: text/html; charset=utf-8');
header("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");

echo <<<EOHTML
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>INCL Tableaux Calculator</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>

    <header>
        <h1>INCL Tableaux Calculator</h1>
        <p>Calculator for tableaux in Graham Priest's Introduction to Non-Classical Logic. <b>"Logic and Software" course</b>, 2024, Faculty of Philosophy, University of Bucharest</p>
    </header>

    <main>
        <div style="float: left; width: 20%">
            <a href="https://www.cambridge.org/core/books/an-introduction-to-nonclassical-logic/61AD69C1D1B88006588B26C37F3A788E" target="_blank">
                <img src="book.jpg" alt="The book" style="width: 50%"/>
            </a>
            
            <br/><br/><b>Select exercise:</b>
            <div class="accordion">$demoProblemsLinks</div>
        </div>

        <div style="float: left; width: 40%;">

            <textarea id="inputTextArea" style="width: 100%; height: 15em;">$input</textarea>

            <div id="onScreenKeyboard"></div>

            <button id="proveButton" style="background-color: #007BFF">PROVE!</button>

            <section id="animation-section"><div id="hand"><div id="yoyo"></div></div></section>

            <b><div id="resultTextArea" style="display: none"></div></b>
            <pre id="resultTreeArea" style="display: none"></pre>
        </div>

        <div style="float: left; width: 40%; height: 80vh">
            <div id="resultPrettyTreeArea" style="width: 100%; height: 100%; display: block"></div>
        </div>
    </main>

    <footer>
        <p>Educational purposes only. Version 1.0. Contact: mc@filos.ro</p>
    </footer>

    <script src="target-js.js"></script>
    <script src="keyboard.js"></script>

    <script src="https://cdn.jsdelivr.net/npm/dagre@0.8.5/dist/dagre.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/cytoscape@3.29.2/dist/cytoscape.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/cytoscape-dagre@2.5.0/cytoscape-dagre.min.js"></script>
    <script>function refreshPrettyTreeUI(contents) { window.refreshPrettyTreeUIImpl(contents); }</script>
    <script src="tree-prettifier.js"></script>
    <script src="yoyo-animation.js"></script>

</body>
</html>
EOHTML;
