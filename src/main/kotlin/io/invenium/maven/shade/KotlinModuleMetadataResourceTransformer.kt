package io.invenium.maven.shade

import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.jvm.KmModuleVisitor
import kotlinx.metadata.jvm.KotlinModuleMetadata
import org.apache.maven.plugins.shade.relocation.Relocator
import org.apache.maven.plugins.shade.resource.ResourceTransformer
import java.io.InputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Resource transformer for merging all *.kotlin-module files.
 *
 * @author Gunnar Schulze
 */
class KotlinModuleMetadataResourceTransformer : ResourceTransformer {

	lateinit var moduleName: String

	private val resourceRegex = Regex("META-INF/.*\\.kotlin_module")

	private val metadata = ArrayList<ByteArray>()

	override fun canTransformResource(resource: String): Boolean {
		return resourceRegex.matches(resource)
	}

	override fun processResource(resource: String, `is`: InputStream, relocators: MutableList<Relocator>) {
		metadata.add(`is`.readBytes())
	}

	override fun hasTransformedResource(): Boolean {
		return metadata.isNotEmpty()
	}

	override fun modifyOutputStream(os: JarOutputStream) {
		os.putNextEntry(ZipEntry("META-INF/$moduleName.kotlin_module"))
		os.write(mergeMetadata())
		os.closeEntry()
	}

	private fun mergeMetadata(): ByteArray {
		val annotations = ArrayList<KmAnnotation>()
		val packageParts = ArrayList<Triple<String, List<String>, Map<String, String>>>()

		for (data in metadata) {
			KotlinModuleMetadata.read(data)!!.accept(object : KmModuleVisitor() {
				override fun visitAnnotation(annotation: KmAnnotation) {
					annotations.add(annotation)
				}

				override fun visitPackageParts(fqName: String, fileFacades: List<String>, multiFileClassParts: Map<String, String>) {
					packageParts.add(Triple(fqName, fileFacades, multiFileClassParts))
				}
			})
		}

		annotations.sortBy { it.className }
		packageParts.sortBy { it.first }

		val writer = KotlinModuleMetadata.Writer()
		for (annotation in annotations) {
			writer.visitAnnotation(annotation)
		}
		for ((fqName, fileFacades, multiFileClassParts) in packageParts) {
			writer.visitPackageParts(fqName, fileFacades, multiFileClassParts)
		}
		return writer.write().bytes
	}
}