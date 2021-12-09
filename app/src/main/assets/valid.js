function() {
    var elem = document.querySelectorAll('.status-container.complete, .complete-image');
    var style = getComputedStyle(elem);
    return (elem);
}

window.onload = setTimeout(start, 100);

function start() {
    document.getElementsByClassName('red test');
    document.body.innerHTML = document.body.innerHTML.replace("flex-container", "");
    clean(document.body.childNodes);
}

function clean(childs)
{
    for (var i = 0; i < childs.length; i++)
    {
        var item = childs[i];

        var itemTag = "";
        if (item.tagName) {
        	itemTag = item.tagName.toLowerCase();
        }

        // Completely skipping SVG
        if (itemTag === 'svg') continue;

        var itemClass = "";
        if (item.className) {
        	itemClass = item.className.toLowerCase();
        }

        // Remove title
        if ((itemTag === 'div') && (itemClass.indexOf('vaccine-result') !== -1)) {
    		item.removeChild(item.firstChild);
        }

        if ((itemTag === 'a') && (itemClass.indexOf('button') !== -1)) {
    		//item.parentElement.removeChild(item);
    		continue;
        }

        clean(item.childNodes);
    }
}