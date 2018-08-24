
function setToCenter(element, dim) {
    var w = $(window).width();
    var h = $(window).height();

    if (dim.width / dim.height > w / h) {
        element.left = 0;
        element.width = w;
        element.height = w * dim.height / dim.width;
        element.top = (h - element.height) / 2;
    } else {
        element.top = 0;
        element.height = h;
        element.width = h * dim.width / dim.height;
        element.left = (w - element.width) / 2;
    }
}

function moveElementTo(element, x, y) {
    var eW = element.width();
    var eH = element.height();
    var bodyW = $(document.body).width();
    var bodyH = $(document.body).height();

    if (x + eW < bodyW) {
        element.css("left", x);
    } else {
        element.css("left", bodyW - eW);
    }
    if (y + eH < bodyH) {
        element.css("top", y);
    } else {
        element.css("top", bodyH - eH);
    }
}

function intensityToColorMap(intensity) {
    if (intensity < config.intensityColorMap.lowIntensityLevel) {
        return config.intensityColorMap.lowIntensityColor;
    }
    if (intensity < config.intensityColorMap.midIntensityLevel) {
        return config.intensityColorMap.midIntensityColor;
    }
    return config.intensityColorMap.highIntensityColor;
}

function toColor(num) {
    num >>>= 0;
    var b = num & 0xFF,
        g = (num & 0xFF00) >>> 8,
        r = (num & 0xFF0000) >>> 16,
        a = ( (num & 0xFF000000) >>> 24 ) / 255 ;
    return "rgba(" + [r, g, b, a].join(",") + ")";
}