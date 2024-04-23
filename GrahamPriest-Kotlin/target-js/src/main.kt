import kotlinx.browser.document

fun main()
{
    val resultDiv = document.getElementById("resultDiv")

    try
    {
        resultDiv?.textContent = test()
    }
    catch (ex : Throwable)
    {
        resultDiv?.textContent = ex.stackTraceToString()
        console.log(ex)
    }
}
