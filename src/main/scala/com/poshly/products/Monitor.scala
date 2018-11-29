package com.poshly.products

import com.poshly.metrics.apm.spi.NewRelicProxy

object Monitor extends com.poshly.metrics.apm.Monitor with NewRelicProxy
