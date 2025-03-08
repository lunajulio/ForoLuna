const RightSidebar = () => {
    return (
      <div className="space-y-6">
        <div>
          <h3 className="text-white font-medium mb-3">Must-read posts</h3>
          <div className="space-y-2">
            <a href="#" className="block text-blue-400 hover:underline text-sm">
              Please read rules before you start working on a platform
            </a>
            <a href="#" className="block text-blue-400 hover:underline text-sm">
              Vision & Strategy of Alemhelp
            </a>
          </div>
        </div>
  
        <div>
          <h3 className="text-white font-medium mb-3">Featured links</h3>
          <div className="space-y-2">
            <a href="#" className="block text-blue-400 hover:underline text-sm">
              Alemhelp source code on GitHub
            </a>
            <a href="#" className="block text-blue-400 hover:underline text-sm">
              Golang best practices
            </a>
          </div>
        </div>
      </div>
    )
  }
  
  export default RightSidebar