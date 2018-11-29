package com.poshly.products.routes

import com.poshly.products.data.BrandsMapper

trait EndRoutesHelper {

  val HTTPS_CDN_BRANDS = "https://7dcf3b0e3767be4ef657-daa6f7956ff3374653f8eaf7fcb1f0a9.ssl.cf2.rackcdn.com/logos/brands/"

  def mapBrandsToLogoURLs(brands: Seq[String]): Seq[BrandsMapper] = {
    brands.map(brand => BrandsMapper(brand, brandToLogoURL(brand)))
  }

  def brandToLogoURL(brand: String): String = {
    HTTPS_CDN_BRANDS + com.poshly.core.Strings.slugify(brand) + ".png"
  }

  def brandToLogoURL(brandOpt: Option[String]): Option[String] = {
    brandOpt.map(brand => HTTPS_CDN_BRANDS + com.poshly.core.Strings.slugify(brand) + ".png")
  }

}
