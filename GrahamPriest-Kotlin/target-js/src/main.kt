import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.url.URLSearchParams

fun main()
{
    val view = View()

    if (URLSearchParams(window.location.search).has("demo_problem"))
    {
        view.clickProveButton()
    }
}

class View
{
    private val inputTextArea = document.getElementById("inputTextArea") as HTMLTextAreaElement
    private val proveButton = document.getElementById("proveButton") as HTMLButtonElement
    private val resultArea = document.getElementById("resultArea") as HTMLElement

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
        resultArea.textContent = "Proving..."
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

            resultArea.textContent = "${problem.description}\n$proof"
            proveButton.hidden = false
        }
        catch (ex : Throwable)
        {
            resultArea.textContent = ex.stackTraceToString()
            proveButton.hidden = false
        }
    }
}
