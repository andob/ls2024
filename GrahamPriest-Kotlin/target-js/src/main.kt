import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement

external fun refreshPrettyTreeUI(contents : String);

fun main()
{
    View()
}

class View
{
    private val inputTextArea = document.getElementById("inputTextArea") as HTMLTextAreaElement
    private val proveButton = document.getElementById("proveButton") as HTMLButtonElement
    private val resultTextArea = document.getElementById("resultTextArea") as HTMLElement
    private val resultTreeArea = document.getElementById("resultTreeArea") as HTMLElement

    init
    {
        proveButton.onclick = eventListener@ {
            onProveButtonClicked()
            return@eventListener Unit
        }
    }

    private fun onProveButtonClicked()
    {
        val inputToProve = inputTextArea.value
        resultTextArea.innerHTML = "Proving..."
        resultTreeArea.textContent = ""
        refreshPrettyTreeUI("")

        window.setTimeout(handler = {
            prove(inputToProve)
        }, timeout = 100)
    }

    private fun prove(inputToProve : String)
    {
        try
        {
            val problem = Problem.fromConfig(inputToProve)
            val proof = problem.prove()

            val (textResult, treeResult) = proof.toStringPair()
            resultTextArea.innerHTML = textResult.toHTMLString()
            resultTreeArea.textContent = treeResult
            refreshPrettyTreeUI(treeResult)
        }
        catch (ex : Throwable)
        {
            resultTextArea.innerHTML = ex.stackTraceToString().toHTMLString()
            resultTreeArea.textContent = ""
            refreshPrettyTreeUI("")
        }
    }

    private fun String.toHTMLString() : String
    {
        return this.replace("\n", "<br/>")
    }
}
