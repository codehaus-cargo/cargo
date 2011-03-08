println "Removing project generated in earlier build"
def directory = new File(basedir, "generate-and-build")

// The complicated code below can be replaced with deleteDir() once
// maven-invoker-plugin 1.6 is released with updated Groovy support (MINVOKER-113).

if (directory.exists())
{
  def dirs = []

  directory.eachFileRecurse
  {
    if (!it.isDirectory())
    {
      it.delete()
    }
    else
    {
      dirs << it
    }
  }

  dirs.reverse().each
  {
    it.delete()
  }

  System.gc()
  Thread.sleep(5000)
}

return true
