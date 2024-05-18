
window.refreshPrettyTreeUIImpl = (rawContents) =>
{
    let resultTreeDiv = document.getElementById('resultPrettyTreeArea');
    let resultTree = rawContents.split(/\r\n|\r|\n/);

    let nodes = [];
    let edges = [];
    let stack = [0];
    let oldIndent = 0;
    let oldId = 0;
    let indentStep = 1;
    for (let id = 0; id < resultTree.length; id++)
    {
        let tokens = resultTree[id].split(/├|─/);
        if (tokens.length > 0)
        {
            let text = tokens[tokens.length-1];
            let indent = tokens.length-1

            nodes.push({
                data: {
                    id: id,
                    text: text
                },
            });

            if (indent > 0 && stack.length > 0)
            {
                if (indent > oldIndent)
                {
                    stack.push(oldId);
                    indentStep = indent - oldIndent;
                }
                else if (indent < oldIndent)
                {
                    let numPops = (oldIndent - indent) / indentStep;
                    for (let i = 0; i < numPops; i++)
                    {
                        stack.pop();
                    }
                }

                edges.push({
                    data: {
                        source: stack[stack.length-1],
                        target: id
                    }
                })
            }

            oldIndent = indent;
            oldId = id;
        }
    }

    cytoscape({
        container: resultTreeDiv,
        elements: {
            nodes: nodes,
            edges: edges,
        },
        layout: {
            name: 'dagre',
        },
        style: [
            {
                selector: 'node',
                style: {
                    'background-color': 'white',
                    'text-valign': 'center',
                    'text-halign': 'center',
                    'label': 'data(text)',
                }
            }
        ],
    });
};
