import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.url.URLSearchParams

external fun refreshPrettyTreeUI(contents : String);

fun main()
{
    val view = View()

    if (URLSearchParams(window.location.search).has("demo_problem"))
    {
        window.setTimeout({ view.clickProveButton() }, 100);
    }
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

    fun clickProveButton()
    {
        onProveButtonClicked()
    }

    private fun onProveButtonClicked()
    {
        val inputToProve = inputTextArea.value
        resultTextArea.innerHTML = "Proving..."
        resultTreeArea.textContent = ""
        refreshPrettyTreeUI("")
        proveButton.hidden = true

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
            proveButton.hidden = false
        }
        catch (ex : Throwable)
        {
            resultTextArea.innerHTML = ex.stackTraceToString().toHTMLString()
            resultTreeArea.textContent = ""
            refreshPrettyTreeUI("")
            proveButton.hidden = false
        }
    }

    private fun String.toHTMLString() : String
    {
        return this.replace("\n", "<br/>")
    }
}
