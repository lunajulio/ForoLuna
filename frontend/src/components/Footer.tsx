import { FaGithub } from 'react-icons/fa'; 

const Footer = () => {
  return (
    <footer className="py-6 border-t border-gray-800">
      <div className="flex items-center justify-center space-x-2">
        <FaGithub className="text-white text-xl" />
        <a 
          href="https://github.com/lunajulio" 
          target="_blank" 
          rel="noopener noreferrer"
          className="text-white hover:text-gray-300 transition-colors"
        >
          @lunajulio
        </a>
      </div>
    </footer>
  );
};

export default Footer;